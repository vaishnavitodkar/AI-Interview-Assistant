import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function AuthPage() {
  const [mode, setMode] = useState('login'); // 'login' | 'register'
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const set = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      let res;
      if (mode === 'login') {
        res = await api.login(form.username, form.password);
      } else {
        res = await api.register(form.username, form.email, form.password);
      }
      login(res.token, res.username);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      {/* Left illustration panel */}
      <div className="auth-left">
        <div className="auth-illustration">
          <h1>
            Ace your next<br />
            <span>tech interview</span>
          </h1>
          <p>
            Practice with AI-generated questions tailored to your role and skill level.
            Get instant, detailed feedback on every answer.
          </p>
          <div className="auth-features">
            {[
              'AI-generated questions for your specific role',
              'Adjustable difficulty — Easy, Medium, Hard',
              'Detailed scorecard with per-question feedback',
              'Track your progress over multiple sessions',
            ].map((f) => (
              <div className="auth-feature" key={f}>
                <span className="auth-feature-dot" />
                {f}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right form panel */}
      <div className="auth-right">
        <div className="auth-box">
          <h2>{mode === 'login' ? 'Welcome back' : 'Create account'}</h2>
          <p className="auth-sub">
            {mode === 'login'
              ? 'Sign in to continue practising'
              : 'Start your interview practice journey'}
          </p>

          {error && (
            <div className="alert alert-error">
              <span>⚠️</span> {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Username</label>
              <input
                className="form-input"
                type="text"
                placeholder="e.g. vaishnavi"
                value={form.username}
                onChange={set('username')}
                required
                autoFocus
              />
            </div>

            {mode === 'register' && (
              <div className="form-group">
                <label className="form-label">Email</label>
                <input
                  className="form-input"
                  type="email"
                  placeholder="you@example.com"
                  value={form.email}
                  onChange={set('email')}
                  required
                />
              </div>
            )}

            <div className="form-group">
              <label className="form-label">Password</label>
              <input
                className="form-input"
                type="password"
                placeholder={mode === 'register' ? 'At least 6 characters' : '••••••••'}
                value={form.password}
                onChange={set('password')}
                required
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-full btn-lg"
              disabled={loading}
              style={{ marginTop: 8 }}
            >
              {loading ? <><span className="spinner" /> Processing…</> : (
                mode === 'login' ? 'Sign in' : 'Create account'
              )}
            </button>
          </form>

          <div className="auth-switch">
            {mode === 'login' ? (
              <>Don't have an account?{' '}
                <a onClick={() => { setMode('register'); setError(''); }}>Sign up</a>
              </>
            ) : (
              <>Already have an account?{' '}
                <a onClick={() => { setMode('login'); setError(''); }}>Sign in</a>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
