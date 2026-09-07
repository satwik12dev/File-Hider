import React from 'react';

export function Input({
  value,
  onChange,
  placeholder = '',
  type = 'text',
  icon = null,
  className = '',
  required = false,
  autoFocus = false,
  style = {},
  ...props
}) {
  return (
    <div style={{ position: 'relative', width: '100%', display: 'flex', alignItems: 'center' }}>
      {icon && (
        <span
          style={{
            position: 'absolute',
            left: '12px',
            color: 'var(--text-muted)',
            pointerEvents: 'none',
            display: 'flex',
            alignItems: 'center'
          }}
        >
          {icon}
        </span>
      )}
      <input
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        autoFocus={autoFocus}
        style={{
          ...style,
          paddingLeft: icon ? '38px' : '14px'
        }}
        className={`clean-input ${className}`}
        {...props}
      />
    </div>
  );
}
