import React, { useState, useEffect, useRef } from 'react';
import { Lock, Mail, User, X, ArrowLeft, RefreshCw } from 'lucide-react';
import { Button } from './ui/Button';
import { Input } from './ui/Input';
import { sendOtpApi, loginApi, signupApi } from '../services/api';
import confetti from 'canvas-confetti';

export default function AuthModal({
  isOpen,
  onClose,
  initialMode = 'login',
  onAuthSuccess,
  onShowToast
}) {
  const [mode, setMode] = useState(initialMode); // 'login' | 'signup'
  const [step, setStep] = useState('email'); // 'email' | 'otp'
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [otp, setOtp] = useState(['', '', '', '']);
  const [loading, setLoading] = useState(false);

  const otpInputsRef = useRef([]);

  useEffect(() => {
    if (isOpen) {
      setMode(initialMode);
      setStep('email');
      setOtp(['', '', '', '']);
    }
  }, [isOpen, initialMode]);

  if (!isOpen) return null;

  const handleSendOtp = async (e) => {
    e.preventDefault();
    if (!email || !email.includes('@')) {
      onShowToast('Please enter a valid email address', 'error');
      return;
    }

    setLoading(true);
    try {
      await sendOtpApi({ email, mode, name });
      onShowToast(`OTP code sent to ${email}`, 'success');
      setStep('otp');
      setTimeout(() => {
        otpInputsRef.current[0]?.focus();
      }, 100);
    } catch (err) {
      onShowToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleOtpChange = (index, val) => {
    if (val && !/^\d+$/.test(val)) return;
    const newOtp = [...otp];
    newOtp[index] = val.slice(-1);
    setOtp(newOtp);

    if (val && index < 3) {
      otpInputsRef.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      otpInputsRef.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').trim();
    if (/^\d{4}$/.test(pasted)) {
      setOtp(pasted.split(''));
      otpInputsRef.current[3]?.focus();
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    const otpCode = otp.join('');
    if (otpCode.length !== 4) {
      onShowToast('Please enter the 4-digit security code', 'error');
      return;
    }

    setLoading(true);
    try {
      let userData;
      if (mode === 'signup') {
        userData = await signupApi({ email, otp: otpCode, name });
      } else {
        userData = await loginApi({ email, otp: otpCode });
      }

      confetti({
        particleCount: 50,
        spread: 60,
        origin: { y: 0.6 }
      });

      onShowToast(`Welcome back, ${userData.name || 'User'}!`, 'success');
      onAuthSuccess({
        email: userData.email || email,
        name: userData.name || name || email.split('@')[0]
      });
      onClose();
    } catch (err) {
      onShowToast(err.message, 'error');
      setOtp(['', '', '', '']);
      otpInputsRef.current[0]?.focus();
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="modal-overlay-backdrop"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div className="clean-modal-box">
        {/* Header */}
        <div className="modal-header-clean">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div style={{ width: '28px', height: '28px', borderRadius: '8px', background: 'var(--accent-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--accent)' }}>
              <Lock style={{ width: '14px', height: '14px' }} />
            </div>
            <h3>Authentication</h3>
          </div>
          <button
            type="button"
            onClick={onClose}
            style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', display: 'flex' }}
          >
            <X style={{ width: '18px', height: '18px' }} />
          </button>
        </div>

        {/* Tab switcher */}
        <div style={{ display: 'flex', background: 'var(--bg-subtle)', border: '1px solid var(--border)', borderRadius: '8px', padding: '3px', marginBottom: '20px' }}>
          <button
            type="button"
            onClick={() => {
              setMode('login');
              setStep('email');
            }}
            style={{
              flex: 1,
              padding: '6px',
              borderRadius: '6px',
              border: 'none',
              background: mode === 'login' ? 'var(--bg-card-solid)' : 'none',
              color: mode === 'login' ? 'var(--text-pure)' : 'var(--text-muted)',
              fontSize: '13px',
              fontWeight: '600',
              cursor: 'pointer',
              boxShadow: mode === 'login' ? '0 1px 2px rgba(0,0,0,0.1)' : 'none'
            }}
          >
            Sign In
          </button>
          <button
            type="button"
            onClick={() => {
              setMode('signup');
              setStep('email');
            }}
            style={{
              flex: 1,
              padding: '6px',
              borderRadius: '6px',
              border: 'none',
              background: mode === 'signup' ? 'var(--bg-card-solid)' : 'none',
              color: mode === 'signup' ? 'var(--text-pure)' : 'var(--text-muted)',
              fontSize: '13px',
              fontWeight: '600',
              cursor: 'pointer',
              boxShadow: mode === 'signup' ? '0 1px 2px rgba(0,0,0,0.1)' : 'none'
            }}
          >
            Register
          </button>
        </div>

        {/* Step 1: Email */}
        {step === 'email' ? (
          <form onSubmit={handleSendOtp} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            {mode === 'signup' && (
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: '600', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                  Your Name
                </label>
                <Input
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Satwik"
                  icon={<User style={{ width: '15px', height: '15px' }} />}
                />
              </div>
            )}

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: '600', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                Email Address
              </label>
              <Input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                icon={<Mail style={{ width: '15px', height: '15px' }} />}
                autoFocus
              />
            </div>

            <Button
              type="submit"
              variant="accent"
              size="md"
              disabled={loading}
              style={{ width: '100%', marginTop: '6px' }}
            >
              {loading ? 'Sending Code...' : 'Send Verification Code'}
            </Button>
          </form>
        ) : (
          /* Step 2: OTP */
          <form onSubmit={handleVerifyOtp} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', textAlign: 'center' }}>
              Enter the 4-digit code sent to <strong style={{ color: 'var(--text-pure)' }}>{email}</strong>
            </p>

            <div className="otp-boxes-wrapper" onPaste={handlePaste}>
              {otp.map((digit, idx) => (
                <input
                  key={idx}
                  ref={(el) => (otpInputsRef.current[idx] = el)}
                  type="text"
                  maxLength={1}
                  value={digit}
                  onChange={(e) => handleOtpChange(idx, e.target.value)}
                  onKeyDown={(e) => handleKeyDown(idx, e)}
                  inputMode="numeric"
                  className="otp-box-clean"
                />
              ))}
            </div>

            <Button
              type="submit"
              variant="accent"
              size="md"
              disabled={loading}
              style={{ width: '100%' }}
            >
              {loading ? 'Verifying...' : 'Unlock Vault'}
            </Button>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '12px' }}>
              <button
                type="button"
                onClick={() => setStep('email')}
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}
              >
                <ArrowLeft style={{ width: '12px', height: '12px' }} /> Back
              </button>
              <button
                type="button"
                onClick={handleSendOtp}
                style={{ background: 'none', border: 'none', color: 'var(--accent)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px', fontWeight: '600' }}
              >
                <RefreshCw style={{ width: '12px', height: '12px' }} /> Resend
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
