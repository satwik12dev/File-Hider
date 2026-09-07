import React from 'react';

export function Card({
  children,
  className = '',
  interactive = true,
  onClick,
  style = {},
  ...props
}) {
  return (
    <div
      onClick={onClick}
      style={style}
      className={`clean-card ${interactive ? 'interactive' : ''} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}
