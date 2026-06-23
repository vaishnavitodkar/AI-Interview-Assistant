import React from 'react';

export default function ScoreRing({ score, size = 120, strokeWidth = 10 }) {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const pct = Math.min(100, Math.max(0, score || 0));
  const offset = circumference - (pct / 100) * circumference;

  const color = pct >= 75 ? '#22C55E' : pct >= 50 ? '#FFB547' : '#FF5C6C';

  return (
    <svg width={size} height={size} style={{ transform: 'rotate(-90deg)' }}>
      <circle
        cx={size / 2} cy={size / 2} r={radius}
        fill="none"
        stroke="rgba(255,255,255,0.15)"
        strokeWidth={strokeWidth}
      />
      <circle
        cx={size / 2} cy={size / 2} r={radius}
        fill="none"
        stroke={color}
        strokeWidth={strokeWidth}
        strokeDasharray={circumference}
        strokeDashoffset={offset}
        strokeLinecap="round"
        style={{ transition: 'stroke-dashoffset 1s ease' }}
      />
      <text
        x="50%" y="50%"
        textAnchor="middle"
        dominantBaseline="central"
        style={{
          transform: 'rotate(90deg)',
          transformOrigin: 'center',
          fill: 'white',
          fontSize: size * 0.22,
          fontWeight: 700,
          fontFamily: 'Space Grotesk, sans-serif',
        }}
      >
        {Math.round(pct)}
      </text>
    </svg>
  );
}
