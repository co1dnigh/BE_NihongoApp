const http = require('http');
const BASE = 'http://localhost:8080';
function api(method, path, token, body) {
  return new Promise((resolve, reject) => {
    const url = new URL(BASE + path);
    const opts = { hostname: url.hostname, port: url.port, path: url.pathname, method, headers: { 'Content-Type': 'application/json' } };
    if (token) opts.headers['Authorization'] = 'Bearer ' + token;
    const req = http.request(opts, (res) => {
      let data = '';
      res.on('data', (ch) => data += ch);
      res.on('end', () => { try { resolve({ status: res.statusCode, body: JSON.parse(data) }); } catch { resolve({ status: res.statusCode, body: data }); } });
    });
    req.on('error', reject);
    if (body) req.write(JSON.stringify(body));
    req.end();
  });
}
(async () => {
  try {
    const login = await api('POST', '/api/v1/auth/login', null, { email: 'test45@test.com', password: 'Test123456' });
    const token = login.body.accessToken;
    console.log('login OK');
    const e1 = await api('GET', '/api/v1/users/me/energy', token);
    console.log('energy before =', JSON.stringify(e1.body));
    const practice = await api('POST', '/api/v1/users/me/energy/practice', token, {});
    console.log('practice =', practice.status, JSON.stringify(practice.body).slice(0, 120));

    const eP = await api('GET', '/api/v1/users/me/energy', token);
    console.log('energy after practice =', JSON.stringify(eP.body));

    const ads = await api('POST', '/api/v1/users/me/energy/ads', token, {});
    console.log('ads =', ads.status, ads.body || '(empty)');
    const eA = await api('GET', '/api/v1/users/me/energy', token);
    console.log('energy after ads =', JSON.stringify(eA.body));
  } catch (e) { console.error('ERR', e && e.message); }
})();
