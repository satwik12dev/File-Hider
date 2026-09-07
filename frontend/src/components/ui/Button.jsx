import React from 'react';

export function Button({
  children,
  variant = 'primary', // 'primary' | 'accent' | 'outline' | 'danger'
  size = 'md', // 'sm' | 'md' | 'lg'
  className = '',
  disabled = false,
  icon = null,
  onClick,
  type = 'button',
  title = '',
  style = {},
  ...props
}) {
  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      title={title}
      style={style}
      className={`clean-btn clean-btn-${variant} clean-btn-${size} ${className}`}
      {...props}
    >
      {icon && <span style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>{icon}</span>}
      <span>{children}</span>
    </button>
  );
}
