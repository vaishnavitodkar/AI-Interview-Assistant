const BASE = '/api';

function getToken() {
  return localStorage.getItem('token');
}

async function request(method, path, body) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(`${BASE}${path}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    });
  } catch (networkErr) {
    throw new Error('Cannot reach the server. Is the backend running on port 8080?');
  }

  
  let data = {};
  try { data = await res.json(); } catch (_) {}

  if (!res.ok) {
    const msg = data.error || data.message || `Request failed (${res.status} ${res.statusText})`;
    const err = new Error(msg);
    err.status = res.status;
    throw err;
  }

  return data;
}

export const api = {
  testAuth: () => request('GET', '/debug/token'),
  testPublic: () => request('GET', '/debug/public'),

  register: (username, email, password) =>
    request('POST', '/auth/register', { username, email, password }),

  login: (username, password) =>
    request('POST', '/auth/login', { username, password }),

  createInterview: (role, difficulty, numberOfQuestions) =>
    request('POST', '/interviews', { role, difficulty, numberOfQuestions }),

  getHistory: () =>
    request('GET', '/interviews'),

  getInterview: (id) =>
    request('GET', `/interviews/${id}`),

  submitAnswer: (interviewId, questionId, answerText) =>
    request('POST', `/interviews/${interviewId}/questions/${questionId}/answer`, { answerText }),

  generateScorecard: (interviewId) =>
    request('POST', `/interviews/${interviewId}/scorecard`),

  getScorecard: (interviewId) =>
    request('GET', `/interviews/${interviewId}/scorecard`),
};
