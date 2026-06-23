import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import ScoreRing from '../components/ScoreRing';

function scoreColor(s) {
  if (s >= 8) return 'var(--success)';
  if (s >= 5) return 'var(--warning)';
  return 'var(--danger)';
}

function scoreLabel(s) {
  if (s >= 80) return 'Excellent';
  if (s >= 65) return 'Good';
  if (s >= 50) return 'Fair';
  return 'Needs work';
}

export default function Scorecard() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [scorecard, setScorecard] = useState(null);
  const [interview, setInterview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([api.getScorecard(id), api.getInterview(id)])
      .then(([sc, iv]) => { setScorecard(sc); setInterview(iv); })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return (
    <div className="page-content">
      <div className="page-loader">
        <div className="spinner spinner-dark" style={{ width: 36, height: 36 }} />
        Loading scorecard…
      </div>
    </div>
  );

  if (error || !scorecard) return (
    <div className="page-content">
      <div className="alert alert-error"><span>⚠️</span>{error || 'Scorecard not found'}</div>
    </div>
  );

  const score = Math.round(scorecard.overallScore);
  const feedback = scorecard.feedback || [];

  return (
    <div className="page-content" style={{ maxWidth: 800 }}>
      <button className="btn btn-ghost btn-sm" onClick={() => navigate('/dashboard')} style={{ marginBottom: 24 }}>
        ← Dashboard
      </button>

      {/* Hero scorecard */}
      <div className="scorecard-hero">
        <div className="scorecard-ring">
          <ScoreRing score={score} size={130} strokeWidth={11} />
        </div>
        <div className="scorecard-hero-text">
          <h2>Interview complete</h2>
          <p>
            {interview?.role} · {interview?.difficulty}
          </p>
          <div className="scorecard-pills">
            <span className="scorecard-pill">
              {scoreLabel(score)} performance
            </span>
            <span className="scorecard-pill">
              {feedback.length} questions evaluated
            </span>
            <span className="scorecard-pill">
              Score: {score}/100
            </span>
          </div>
        </div>
      </div>

      {/* Strengths & Weaknesses */}
      <div className="strength-weakness-grid">
        <div className="sw-card strength">
          <h4>💪 Strengths</h4>
          <p>{scorecard.strengths || 'No specific strengths noted.'}</p>
        </div>
        <div className="sw-card weakness">
          <h4>📈 Areas to improve</h4>
          <p>{scorecard.weaknesses || 'No specific weaknesses noted.'}</p>
        </div>
      </div>

      {/* Per-question feedback */}
      {feedback.length > 0 && (
        <>
          <h3 style={{ fontFamily: 'var(--font-display)', fontWeight: 700, marginBottom: 16, fontSize: '1.1rem' }}>
            Question-by-question feedback
          </h3>
          <div className="feedback-list">
            {feedback.map((item, idx) => (
              <div key={item.questionId || idx} className="feedback-item">
                <div className="feedback-item-header">
                  <div>
                    <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', marginBottom: 4 }}>
                      Q{idx + 1}
                    </div>
                    <div className="feedback-question">{item.questionText}</div>
                  </div>
                  <div className="feedback-score" style={{ color: scoreColor(item.score) }}>
                    {item.score?.toFixed(1)}
                    <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 400 }}>/10</span>
                  </div>
                </div>

                {/* Mini score bar */}
                <div className="progress-bar-wrap" style={{ marginBottom: 10, height: 5 }}>
                  <div
                    className="progress-bar-fill"
                    style={{
                      width: `${(item.score / 10) * 100}%`,
                      background: scoreColor(item.score),
                    }}
                  />
                </div>

                <div className="feedback-comment">{item.comment}</div>
              </div>
            ))}
          </div>
        </>
      )}

      {/* Actions */}
      <div style={{ display: 'flex', gap: 12, marginTop: 32, flexWrap: 'wrap' }}>
        <button className="btn btn-primary" onClick={() => navigate('/create')}>
          + Practice again
        </button>
        <button className="btn btn-ghost" onClick={() => navigate('/dashboard')}>
          Back to dashboard
        </button>
      </div>
    </div>
  );
}
