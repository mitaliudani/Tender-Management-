import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {resolve} from 'node:path';

const repo = resolve(import.meta.dirname, '..');
/** Reads a source file relative to the fork root for static regression assertions. */
const read = path => readFileSync(resolve(repo, path), 'utf8');

/** Ensures each affected admin mutation is submitted by a CSRF-bearing POST form. */
test('three sensitive actions use POST forms with synchronizer tokens', () => {
  const cases = [
    ['viewTenderBidsForm.jsp', 'AcceptBidSrv'],
    ['viewTenderBidsForm.jsp', 'RejectBidSrv'],
    ['removeNotice.jsp', 'RemoveNoticeSrv'],
  ];
  for (const [page, servlet] of cases) {
    const source = read(`tendermanagement/WebContent/${page}`);
    const action = new RegExp(`<form\\s+action="${servlet}"\\s+method="post">([\\s\\S]*?)<\\/form>`, 'i');
    const match = action.exec(source);
    assert.ok(match, `${page} must POST to ${servlet}`);
    assert.match(match[1], /name="csrfToken"\s+value="<%=CsrfTokens\.getOrCreate\(session\)%>"/);
    assert.doesNotMatch(source, new RegExp(`href="${servlet}\\?`));
  }
});

/** Confirms all three sensitive URL mappings enforce method, session, and token checks. */
test('the servlet filter has all mappings and checks method, role, and token', () => {
  const filter = read('tendermanagement/src/com/hit/security/MutatingActionFilter.java');
  for (const action of ['AcceptBidSrv', 'RejectBidSrv', 'RemoveNoticeSrv']) {
    assert.ok(filter.includes(`"/${action}"`), `Missing filter mapping ${action}`);
    const handler = read(`tendermanagement/src/com/hit/srv/${action}.java`);
    assert.match(handler, /request\.getSession\(false\)/);
    assert.match(handler, /response\.sendError\(HttpServletResponse\.SC_FORBIDDEN\);\s*return;/);
  }
  assert.match(filter, /"POST"\.equals\(request\.getMethod\(\)\)/);
  assert.match(filter, /CsrfTokens\.verify\(session, request\.getParameter\("csrfToken"\)\)/);
});

/** Confirms protected views reject absent admins before showing sensitive content. */
test('the two protected JSPs fail closed without session passwords', () => {
  for (const page of ['viewTenderBidsForm.jsp', 'removeNotice.jsp']) {
    const source = read(`tendermanagement/WebContent/${page}`);
    assert.match(source, /!"admin"\.equalsIgnoreCase\(user\)\s*\|\|\s*uname\s*==\s*null/);
    assert.match(source, /response\.sendRedirect\("loginFailed\.jsp"\);\s*return;/);
    assert.doesNotMatch(source, /session\.getAttribute\("password"\)|\bpword\b/);
  }
});

/** Confirms output in the modified views and status handlers is HTML-escaped. */
test('affected views and action status outputs are HTML-encoded', () => {
  const bids = read('tendermanagement/WebContent/viewTenderBidsForm.jsp');
  const notices = read('tendermanagement/WebContent/removeNotice.jsp');
  assert.match(bids, /HtmlEscaper\.escape\(request\.getParameter\("tid"\)\)/);
  assert.match(bids, /HtmlEscaper\.escape\(bidder\.getBidStatus\(\)\)/);
  assert.match(notices, /HtmlEscaper\.escape\(noticeTitle\)/);
  assert.match(notices, /HtmlEscaper\.escape\(noticeDesc\)/);
  for (const action of ['AcceptBidSrv', 'RejectBidSrv', 'RemoveNoticeSrv']) {
    const handler = read(`tendermanagement/src/com/hit/srv/${action}.java`);
    assert.match(handler, /HtmlEscaper\.escape\(status\)/);
    assert.doesNotMatch(handler, /innerHTML/);
  }
});
