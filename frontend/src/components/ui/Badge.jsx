import React from 'react';

export function Badge({
  children,
  variant = 'emerald',
  pulse = false,
  className = ''
}) {
  const variantStyles = {
    emerald: 'bg-[#00ff87]/10 text-[#00ff87] border-[#00ff87]/35 shadow-[0_2px_8px_rgba(0,255,135,0.25)]',
    cyan: 'bg-[#00f0ff]/10 text-[#00f0ff] border-[#00f0ff]/35 shadow-[0_2px_8px_rgba(0,240,255,0.25)]',
    purple: 'bg-purple-500/10 text-purple-300 border-purple-500/35 shadow-[0_2px_8px_rgba(168,85,247,0.2)]',
    amber: 'bg-amber-500/10 text-amber-300 border-amber-500/35 shadow-[0_2px_8px_rgba(245,158,11,0.2)]',
    rose: 'bg-rose-500/10 text-rose-300 border-rose-500/35 shadow-[0_2px_8px_rgba(244,63,94,0.2)]',
    neutral: 'bg-white/5 text-[var(--text-secondary)] border-[var(--border-zinc)]'
  }[variant] || '';

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full font-mono text-[11px] font-bold border tracking-wider ${variantStyles} ${className}`}
    >
      {pulse && (
        <span className="w-1.5 h-1.5 rounded-full bg-current shadow-[0_0_8px_currentColor] animate-pulse" />
      )}
      {children}
    </span>
  );
}
