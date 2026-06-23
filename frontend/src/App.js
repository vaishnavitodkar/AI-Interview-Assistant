import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import AuthPage from './pages/AuthPage';
import Dashboard from './pages/Dashboard';
import CreateInterview from './pages/CreateInterview';
import InterviewSession from './pages/InterviewSession';
import Scorecard from './pages/Scorecard';

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return (
    <div className="flex-center" style={{ minHeight: '100vh' }}>
      <div className="spinner spinner-dark" style={{ width: 36, height: 36 }} />
    </div>
  );
  return user ? children : <Navigate to="/login" replace />;
}

function AppShell({ children }) {
  const { user } = useAuth();
  return (
    <div className="app-shell">
      {user && <Navbar />}
      {children}
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppShell>
          <Routes>
            <Route path="/login" element={<AuthPage />} />
            <Route path="/dashboard" element={
              <ProtectedRoute><Dashboard /></ProtectedRoute>
            } />
            <Route path="/create" element={
              <ProtectedRoute><CreateInterview /></ProtectedRoute>
            } />
            <Route path="/interview/:id" element={
              <ProtectedRoute><InterviewSession /></ProtectedRoute>
            } />
            <Route path="/scorecard/:id" element={
              <ProtectedRoute><Scorecard /></ProtectedRoute>
            } />
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </AppShell>
      </BrowserRouter>
    </AuthProvider>
  );
}
