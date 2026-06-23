import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';

const DIFFICULTY_COLOR = { EASY: 'badge-accent', MEDIUM: 'badge-warning', HARD: 'badge-danger' };
const STATUS_COLOR = { CREATED: 'badge-neutral', IN_PROGRESS: 'badge-primary', COMPLETED: 'badge-success' };
const STATUS_LABEL = { CREATED: 'Not started', IN_PROGRESS: 'In progress', COMPLETED: 'Completed' };

function formatDate(dt) {
  if (!dt) return '';
  return new Date(dt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}

export default function Dashboard() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    api.getHistory()
      .then(setHistory)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const completed = history.filter((i) => i.status === 'COMPLETED');
  const avgScore = completed.length
    ? Math.round(completed.reduce((s, i) => s + (i.overallScore || 0), 0) / completed.length)
    : null;
  const inProgress = history.filter((i) => i.status === 'IN_PROGRESS').length;

  return (
    <div className="page-content">
      {/* Greeting */}
      <div style={{ marginBottom: 28 }}>
        <h1 className="section-title">
          Hey, {user?.username} 👋
        </h1>
        <p className="section-subtitle">
          {history.length === 0
            ? 'Start your first mock interview to get AI-powered feedback.'
            : 'Keep practising — consistency is the key to landing that offer.'}
        </p>
      </div>

      {/* Stats */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon stat-icon-purple">🎯</div>
          <div>
            <div className="stat-label">Total interviews</div>
            <div className="stat-value">{history.length}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon stat-icon-green">✅</div>
          <div>
            <div className="stat-label">Avg score</div>
            <div className="stat-value">{avgScore !== null ? `${avgScore}` : '—'}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon stat-icon-orange">⏳</div>
          <div>
            <div className="stat-label">In progress</div>
            <div className="stat-value">{inProgress}</div>
          </div>
        </div>
      </div>

      {/* CTA */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ fontFamily: 'var(--font-display)', fontSize: '1.1rem', fontWeight: 700 }}>
          Interview history
        </h2>
        <button className="btn btn-primary" onClick={() => navigate('/create')}>
          + New interview
        </button>
      </div>

      {error && <div className="alert alert-error"><span>⚠️</span>{error}</div>}

      {loading ? (
        <div className="page-loader">
          <div className="spinner spinner-dark" />
          Loading your interviews…
        </div>
      ) : history.length === 0 ? (
        <div className="empty-state card">
          <div className="card-body">
            <span className="empty-icon">🚀</span>
            <h3>No interviews yet</h3>
            <p>Create your first mock interview and start practising with AI-generated questions.</p>
            <button className="btn btn-primary" onClick={() => navigate('/create')}>
              Start first interview
            </button>
          </div>
        </div>
      ) : (
        <div className="interview-list">
          {history.map((interview) => (
            <div
              key={interview.id}
              className="interview-card"
              onClick={() => navigate(`/interview/${interview.id}`)}
            >
              <div className="interview-card-icon">💼</div>
              <div className="interview-card-body">
                <div className="interview-card-role">{interview.role}</div>
                <div className="interview-card-meta">
                  <span className={`badge ${DIFFICULTY_COLOR[interview.difficulty]}`}>
                    {interview.difficulty}
                  </span>
                  <span className={`badge ${STATUS_COLOR[interview.status]}`}>
                    {STATUS_LABEL[interview.status]}
                  </span>
                  <span>{formatDate(interview.createdAt)}</span>
                </div>
              </div>
              {interview.overallScore != null && (
                <div className="interview-card-score">
                  {Math.round(interview.overallScore)}
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 400 }}>/100</span>
                </div>
              )}
              <span style={{ color: 'var(--text-muted)', fontSize: '1.1rem' }}>›</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
