const http = require('http');

function api(method, path, token, body) {
  return new Promise((resolve, reject) => {
    const url = path.startsWith('http') ? new URL(path) : new URL('http://localhost:8080' + path);
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
  const login = await api('POST', '/api/v1/auth/login', null, { email: 'test45@test.com', password: 'Test123456' });
  const token = login.body.accessToken;
  console.log('login OK userId=', login.body.user.id);

  const energyBefore = await api('GET', '/api/v1/users/me/energy', token);
  console.log('energy before=', JSON.stringify(energyBefore.body));

  const start = await api('POST', '/api/v1/lessons/1/start', token, {});
  console.log('start status=', start.status, 'keys=', Object.keys(start.body || {}).join(',') || typeof start.body);
  if (start.body && Array.isArray(start.body.questions)) {
    console.log('questions count=', start.body.questions.length);
    start.body.questions.forEach(q => console.log(' q', q.questionId, q.content || q.questionText || ''));
  } else {
    console.log('start body=', JSON.stringify(start.body).slice(0, 400));
  }

  if (start.body && Array.isArray(start.body.questions) && start.body.questions.length) {
    const answers = start.body.questions.map(q => ({ questionId: q.questionId, isCorrect: true }));
    const submit = await api('POST', '/api/v1/lessons/1/submit', token, { answers, totalQuestions: answers.length, totalCorrect: answers.length, totalMistakes: 0, isReplay: false });
    console.log('submit status=', submit.status);
    if (submit.body) {
      console.log('submit statusField=', submit.body.status);
      console.log('submit energy=', submit.body.currentEnergy);
      console.log('submit exp=', submit.body.expEarned);
    }
    const energyAfter = await api('GET', '/api/v1/users/me/energy', token);
    console.log('energy after =', JSON.stringify(energyAfter.body));
  }
})();
