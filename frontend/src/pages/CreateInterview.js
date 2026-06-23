import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';

const DIFFICULTIES = [
  { value: 'EASY',   emoji: '🌱', label: 'Easy',   desc: 'Core concepts & fundamentals' },
  { value: 'MEDIUM', emoji: '🔥', label: 'Medium', desc: 'Applied knowledge & scenarios' },
  { value: 'HARD',   emoji: '⚡', label: 'Hard',   desc: 'System design & deep dives' },
];

const ROLE_SUGGESTIONS = [
  'Backend Developer - Java',
  'Full Stack Developer',
  'Frontend Developer - React',
  'DevOps Engineer',
  'Data Engineer',
  'Software Engineer - Node.js',
];

export default function CreateInterview() {
  const [role, setRole] = useState('');
  const [difficulty, setDifficulty] = useState('MEDIUM');
  const [questionCount, setQuestionCount] = useState(5);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!role.trim()) { setError('Please enter a role.'); return; }
    setError('');
    setLoading(true);
    try {
      const interview = await api.createInterview(role.trim(), difficulty, questionCount);
      navigate(`/interview/${interview.id}`);
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  return (
    <div className="page-content" style={{ maxWidth: 640 }}>
      <button className="btn btn-ghost btn-sm" onClick={() => navigate('/dashboard')} style={{ marginBottom: 24 }}>
        ← Back
      </button>

      <h1 className="section-title">New mock interview</h1>
      <p className="section-subtitle">
        Tell us the role and difficulty — our AI will generate tailored questions.
      </p>

      {error && <div className="alert alert-error"><span>⚠️</span>{error}</div>}

      {loading ? (
        <div className="card">
          <div className="card-body" style={{ textAlign: 'center', padding: '60px 24px' }}>
            <div className="page-loader">
              <div className="spinner spinner-dark" style={{ width: 36, height: 36 }} />
              <strong style={{ color: 'var(--text-primary)', fontSize: '1rem' }}>
                Generating your questions…
              </strong>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginTop: 4 }}>
                Our AI is crafting {questionCount} questions for <em>{role}</em>. This takes a few seconds.
              </p>
            </div>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit}>
          {/* Role input */}
          <div className="card" style={{ marginBottom: 16 }}>
            <div className="card-body">
              <div className="form-group" style={{ marginBottom: 12 }}>
                <label className="form-label">Target role</label>
                <input
                  className="form-input"
                  type="text"
                  placeholder="e.g. Backend Developer - Java"
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                  required
                  autoFocus
                />
                <div className="form-hint">Be specific — the AI uses this to generate relevant questions.</div>
              </div>

              {/* Quick suggestions */}
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                {ROLE_SUGGESTIONS.map((s) => (
                  <button
                    key={s}
                    type="button"
                    className="btn btn-ghost btn-sm"
                    style={{ fontSize: '0.78rem', padding: '4px 12px' }}
                    onClick={() => setRole(s)}
                  >
                    {s}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Difficulty picker */}
          <div className="card" style={{ marginBottom: 16 }}>
            <div className="card-body">
              <label className="form-label" style={{ marginBottom: 12 }}>Difficulty</label>
              <div className="difficulty-picker">
                {DIFFICULTIES.map((d) => (
                  <div
                    key={d.value}
                    className={`difficulty-option ${difficulty === d.value ? 'selected' : ''}`}
                    onClick={() => setDifficulty(d.value)}
                  >
                    <span className="diff-emoji">{d.emoji}</span>
                    <div className="diff-label">{d.label}</div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: 2 }}>{d.desc}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Question count */}
          <div className="card" style={{ marginBottom: 24 }}>
            <div className="card-body">
              <label className="form-label">
                Number of questions
                <span style={{ color: 'var(--primary)', marginLeft: 8, fontFamily: 'var(--font-display)' }}>
                  {questionCount}
                </span>
              </label>
              <input
                type="range"
                min={3}
                max={10}
                value={questionCount}
                onChange={(e) => setQuestionCount(Number(e.target.value))}
                style={{ width: '100%', accentColor: 'var(--primary)', marginTop: 8 }}
              />
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: 4 }}>
                <span>3 (quick)</span>
                <span>10 (thorough)</span>
              </div>
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-full btn-lg">
            Generate interview →
          </button>
        </form>
      )}
    </div>
  );
}
