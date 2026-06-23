import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const initials = user?.username
    ? user.username.slice(0, 2).toUpperCase()
    : '?';

  return (
    <nav className="navbar">
      <div
        className="navbar-brand"
        style={{ cursor: 'pointer' }}
        onClick={() => navigate('/dashboard')}
      >
        <div className="brand-icon">🎯</div>
        InterviewAI
      </div>

      <div className="navbar-actions">
        {user && (
          <>
            <div className="nav-user">
              <div className="nav-avatar">{initials}</div>
              <span>{user.username}</span>
            </div>
            <button className="btn btn-ghost btn-sm" onClick={handleLogout}>
              Sign out
            </button>
          </>
        )}
      </div>
    </nav>
  );
}
