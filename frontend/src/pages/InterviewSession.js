import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';

const DIFF_BADGE = { EASY: 'badge-accent', MEDIUM: 'badge-warning', HARD: 'badge-danger' };

export default function InterviewSession() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [interview, setInterview] = useState(null);
  const [activeIdx, setActiveIdx] = useState(0);
  const [draft, setDraft] = useState('');
  const [saving, setSaving] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [saved, setSaved] = useState(false);

  const loadInterview = useCallback(async () => {
    try {
      const data = await api.getInterview(id);
      setInterview(data);
      
      const q = data.questions[activeIdx];
      if (q?.answerText) setDraft(q.answerText);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [id, activeIdx]);

  useEffect(() => { loadInterview(); }, [id]);  

  
  const goTo = (idx) => {
    setActiveIdx(idx);
    setSaved(false);
    setError('');
    const q = interview?.questions[idx];
    setDraft(q?.answerText || '');
  };

  const saveAnswer = async () => {
    const q = interview.questions[activeIdx];
    if (!draft.trim()) { setError('Please write an answer before saving.'); return; }
    setError('');
    setSaving(true);
    try {
      const updated = await api.submitAnswer(id, q.id, draft);
      setInterview(updated);
      setSaved(true);
    } catch (e) {
      setError(e.message);
    } finally {
      setSaving(false);
    }
  };

  const allAnswered = interview?.questions.every((q) => q.answerText);

  const finishAndScore = async () => {
    setError('');
    setSubmitting(true);
    try {
      await api.generateScorecard(id);
      navigate(`/scorecard/${id}`);
    } catch (e) {
      setError(e.message);
      setSubmitting(false);
    }
  };

  if (loading) return (
    <div className="page-content">
      <div className="page-loader">
        <div className="spinner spinner-dark" />
        Loading interview…
      </div>
    </div>
  );

  if (!interview) return (
    <div className="page-content">
      <div className="alert alert-error"><span>⚠️</span>{error || 'Interview not found'}</div>
    </div>
  );

  const questions = interview.questions;
  const current = questions[activeIdx];
  const answeredCount = questions.filter((q) => q.answerText).length;
  const progress = Math.round((answeredCount / questions.length) * 100);

  // If already completed, redirect to scorecard
  if (interview.status === 'COMPLETED') {
    return (
      <div className="page-content" style={{ maxWidth: 640 }}>
        <div className="card">
          <div className="card-body" style={{ textAlign: 'center', padding: '48px 24px' }}>
            <span style={{ fontSize: '3rem', display: 'block', marginBottom: 16 }}>🏆</span>
            <h2 style={{ fontFamily: 'var(--font-display)', marginBottom: 8 }}>Interview completed!</h2>
            <p style={{ color: 'var(--text-secondary)', marginBottom: 24 }}>
              Your scorecard has already been generated.
            </p>
            <button className="btn btn-primary" onClick={() => navigate(`/scorecard/${id}`)}>
              View scorecard
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-content" style={{ maxWidth: 800 }}>
      {/* Session header */}
      <div className="session-header">
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12, flexWrap: 'wrap', gap: 8 }}>
          <div>
            <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '1.05rem' }}>
              {interview.role}
            </div>
            <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
              <span className={`badge ${DIFF_BADGE[interview.difficulty]}`}>{interview.difficulty}</span>
              <span className="badge badge-neutral">{questions.length} questions</span>
            </div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '1.2rem', color: 'var(--primary)' }}>
              {answeredCount}/{questions.length}
            </div>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>answered</div>
          </div>
        </div>
        <div className="session-progress-label">
          <span>Progress</span>
          <span>{progress}%</span>
        </div>
        <div className="progress-bar-wrap">
          <div className="progress-bar-fill" style={{ width: `${progress}%` }} />
        </div>
      </div>

      {/* Question navigator dots */}
      <div className="questions-nav">
        {questions.map((q, idx) => (
          <button
            key={q.id}
            className={`q-nav-dot ${q.answerText ? 'answered' : ''} ${idx === activeIdx ? 'active' : ''}`}
            onClick={() => goTo(idx)}
            title={`Question ${idx + 1}${q.answerText ? ' — answered' : ''}`}
          >
            {idx + 1}
          </button>
        ))}
      </div>

      {error && <div className="alert alert-error"><span>⚠️</span>{error}</div>}

      {/* Current question */}
      <div className="question-card">
        <div className="question-number">Question {activeIdx + 1} of {questions.length}</div>
        <div className="question-text">{current.questionText}</div>

        <label className="form-label">Your answer</label>
        <textarea
          className="form-textarea"
          rows={6}
          placeholder="Type your answer here… Be thorough — the AI evaluates depth and accuracy."
          value={draft}
          onChange={(e) => { setDraft(e.target.value); setSaved(false); }}
        />

        {saved && (
          <div className="answer-status">
            <span>✅</span> Answer saved
          </div>
        )}

        <div style={{ display: 'flex', gap: 10, marginTop: 16, flexWrap: 'wrap' }}>
          <button
            className="btn btn-primary"
            onClick={saveAnswer}
            disabled={saving || !draft.trim()}
          >
            {saving ? <><span className="spinner" />Saving…</> : '💾 Save answer'}
          </button>

          {activeIdx < questions.length - 1 && (
            <button
              className="btn btn-secondary"
              onClick={() => { if (draft.trim() && !saved) saveAnswer().then(() => goTo(activeIdx + 1)); else goTo(activeIdx + 1); }}
            >
              Next question →
            </button>
          )}

          {activeIdx > 0 && (
            <button className="btn btn-ghost" onClick={() => goTo(activeIdx - 1)}>
              ← Previous
            </button>
          )}
        </div>
      </div>

      {/* Finish button */}
      {allAnswered && (
        <div className="card" style={{ marginTop: 8, border: '1.5px solid var(--primary)' }}>
          <div className="card-body" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16, flexWrap: 'wrap' }}>
            <div>
              <div style={{ fontWeight: 600, marginBottom: 4 }}>All questions answered! 🎉</div>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                Submit to get your AI-generated scorecard with detailed feedback.
              </div>
            </div>
            <button
              className="btn btn-primary btn-lg"
              onClick={finishAndScore}
              disabled={submitting}
            >
              {submitting
                ? <><span className="spinner" />Generating scorecard…</>
                : '🏆 Get my score'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
