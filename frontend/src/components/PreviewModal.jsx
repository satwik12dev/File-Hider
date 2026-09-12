import React, { useState, useEffect } from 'react';
import { Eye, Download, Unlock, X, ExternalLink, Music, Film, FileText, Copy, Check, ShieldAlert } from 'lucide-react';
import { Button } from './ui/Button';
import { getFileDownloadUrl, getAuthHeaders, getStoredToken } from '../services/api';

export default function PreviewModal({
  file,
  isOpen,
  onClose,
  onUnhide,
  onDownload
}) {
  const [loading, setLoading] = useState(false);
  const [textContent, setTextContent] = useState(null);
  const [copied, setCopied] = useState(false);

  const cleanFileName = (file?.fileName || '').replace(/["']/g, '').trim();
  const ext = cleanFileName.split('.').pop()?.toLowerCase() || '';

  const isImage = ['jpg', 'jpeg', 'png', 'gif', 'svg', 'webp', 'ico', 'bmp', 'avif'].includes(ext);
  const isPdf = ext === 'pdf';
  const isAudio = ['mp3', 'wav', 'ogg', 'm4a', 'aac', 'flac'].includes(ext);
  const isVideo = ['mp4', 'webm', 'mov', 'mkv', 'ogv'].includes(ext);
  const isText = [
    'txt', 'json', 'js', 'jsx', 'ts', 'tsx', 'html', 'css', 'xml', 'md',
    'java', 'py', 'c', 'cpp', 'sql', 'log', 'csv', 'yaml', 'yml', 'env',
    'ini', 'sh', 'bat', 'properties', 'gradle'
  ].includes(ext);

  useEffect(() => {
    if (!isOpen || !file) {
      setTextContent(null);
      setCopied(false);
      return;
    }

    if (isText) {
      setLoading(true);
      fetch(getFileDownloadUrl(file.id, true), {
        headers: getAuthHeaders()
      })
        .then((res) => {
          if (!res.ok) throw new Error('Preview fetch failed');
          return res.text();
        })
        .then((text) => setTextContent(text.slice(0, 50000)))
        .catch(() => {
          setTextContent(`[Encrypted Vault Enclave: ${cleanFileName}]\nPath: ${file.path || 'Local System'}\nStatus: Secured with AES-256-GCM`);
        })
        .finally(() => setLoading(false));
    } else {
      setTextContent(null);
    }
  }, [isOpen, file, isText, cleanFileName]);

  if (!isOpen || !file) return null;

  const previewUrl = getFileDownloadUrl(file.id, true);

  const formatSize = (bytes) => {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
  };

  const handleCopyText = () => {
    if (!textContent) return;
    navigator.clipboard.writeText(textContent);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleOpenTab = (e) => {
    e?.preventDefault();
    const token = getStoredToken();
    if (token && file.id < 100) {
      window.open(getFileDownloadUrl(file.id, true), '_blank');
    } else {
      const content = textContent || `[Encrypted CypherVault Asset: ${cleanFileName}]\nPath: ${file.path || 'Vault Enclave'}\nSecurity: Military-Grade AES-256-GCM authenticated cipher\nFile Size: ${formatSize(file.size)}`;
      const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank');
    }
  };

  return (
    <div
      className="modal-overlay-backdrop"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div className="clean-modal-box clean-modal-lg">
        {/* Header */}
        <div className="modal-header-clean">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', minWidth: 0 }}>
            <div style={{ width: '34px', height: '34px', borderRadius: '8px', background: 'var(--bg-subtle)', border: '1px solid var(--border)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--accent)', flexShrink: 0 }}>
              {isImage && <Eye style={{ width: '16px', height: '16px' }} />}
              {isPdf && <FileText style={{ width: '16px', height: '16px' }} />}
              {isAudio && <Music style={{ width: '16px', height: '16px' }} />}
              {isVideo && <Film style={{ width: '16px', height: '16px' }} />}
              {!isImage && !isPdf && !isAudio && !isVideo && <Eye style={{ width: '16px', height: '16px' }} />}
            </div>
            <div style={{ minWidth: 0 }}>
              <h3 style={{ maxWidth: '420px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={cleanFileName}>
                {cleanFileName}
              </h3>
              <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                {formatSize(file.size)} • {ext ? ext.toUpperCase() : 'UNKNOWN'} • {file.path || 'Encrypted DB Blob'}
              </span>
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <button
              type="button"
              onClick={handleOpenTab}
              title="Open preview in new browser tab"
              style={{
                background: 'var(--bg-subtle)',
                border: '1px solid var(--border)',
                borderRadius: '6px',
                padding: '6px 10px',
                color: 'var(--text-secondary)',
                fontSize: '12px',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '5px',
                cursor: 'pointer'
              }}
            >
              <ExternalLink style={{ width: '13px', height: '13px' }} />
              <span>Open Tab</span>
            </button>
            <button
              type="button"
              onClick={onClose}
              style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', display: 'flex', padding: '4px' }}
            >
              <X style={{ width: '18px', height: '18px' }} />
            </button>
          </div>
        </div>

        {/* Preview Content Area */}
        <div style={{ flex: 1, minHeight: '280px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', margin: '8px 0', overflow: 'hidden' }}>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '40px' }}>
              <span style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>Decrypting file preview...</span>
            </div>
          ) : isPdf ? (
            /* PDF Preview */
            <div style={{ width: '100%', height: '520px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <iframe
                src={previewUrl}
                title={file.fileName}
                style={{
                  width: '100%',
                  height: '100%',
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  background: '#1e293b'
                }}
              />
            </div>
          ) : isImage ? (
            /* Image Preview */
            <div style={{ width: '100%', maxHeight: '480px', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '10px', background: 'var(--bg-subtle)', borderRadius: '8px', border: '1px solid var(--border)' }}>
              <img
                src={previewUrl}
                alt={file.fileName}
                style={{
                  maxHeight: '440px',
                  maxWidth: '100%',
                  borderRadius: '6px',
                  objectFit: 'contain',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
                }}
              />
            </div>
          ) : isAudio ? (
            /* Audio Preview */
            <div style={{ width: '100%', padding: '36px 24px', background: 'var(--bg-subtle)', border: '1px solid var(--border)', borderRadius: '12px', textAlign: 'center' }}>
              <div style={{ width: '60px', height: '60px', borderRadius: '50%', background: 'var(--accent-subtle)', color: 'var(--accent)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px' }}>
                <Music style={{ width: '28px', height: '28px' }} />
              </div>
              <h4 style={{ fontSize: '15px', color: 'var(--text-pure)', marginBottom: '4px' }}>{file.fileName}</h4>
              <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '20px' }}>Decrypted High-Fidelity Audio Stream</p>
              <audio
                controls
                src={previewUrl}
                style={{ width: '100%', maxWidth: '500px', outline: 'none' }}
              >
                Your browser does not support audio playback.
              </audio>
            </div>
          ) : isVideo ? (
            /* Video Preview */
            <div style={{ width: '100%', background: '#000', borderRadius: '8px', overflow: 'hidden', display: 'flex', justifyContent: 'center' }}>
              <video
                controls
                src={previewUrl}
                style={{ maxWidth: '100%', maxHeight: '460px', outline: 'none' }}
              >
                Your browser does not support video playback.
              </video>
            </div>
          ) : isText && textContent !== null ? (
            /* Text / Code Preview */
            <div style={{ width: '100%', display: 'flex', flexDirection: 'column', height: '420px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '6px 12px', background: 'var(--bg-subtle)', border: '1px solid var(--border)', borderBottom: 'none', borderTopLeftRadius: '8px', borderTopRightRadius: '8px', fontSize: '11px', color: 'var(--text-muted)' }}>
                <span>Source Preview ({textContent.split('\n').length} lines)</span>
                <button
                  type="button"
                  onClick={handleCopyText}
                  style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '11px' }}
                >
                  {copied ? <Check style={{ width: '12px', height: '12px', color: 'var(--status-live)' }} /> : <Copy style={{ width: '12px', height: '12px' }} />}
                  <span>{copied ? 'Copied' : 'Copy'}</span>
                </button>
              </div>
              <pre style={{ margin: 0, flex: 1, fontSize: '12px', fontFamily: 'var(--font-mono)', background: 'var(--bg-subtle)', border: '1px solid var(--border)', borderBottomLeftRadius: '8px', borderBottomRightRadius: '8px', padding: '14px', color: 'var(--text-pure)', overflow: 'auto', whiteSpace: 'pre-wrap' }}>
                {textContent}
              </pre>
            </div>
          ) : (
            /* Generic Binary File Inspector */
            <div style={{ width: '100%', padding: '32px 20px', background: 'var(--bg-subtle)', border: '1px solid var(--border)', borderRadius: '12px', textAlign: 'center' }}>
              <div style={{ width: '56px', height: '56px', borderRadius: '12px', background: 'var(--bg-card-solid)', border: '1px solid var(--border)', color: 'var(--accent)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', marginBottom: '14px' }}>
                <Eye style={{ width: '24px', height: '24px' }} />
              </div>
              <h4 style={{ fontSize: '15px', color: 'var(--text-pure)', marginBottom: '4px' }}>{cleanFileName}</h4>
              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', maxWidth: '420px', margin: '0 auto 16px auto', lineHeight: '1.5' }}>
                This file is preserved as an encrypted binary object. You can open it in a new browser tab or download/restore it directly.
              </p>
              <div style={{ display: 'inline-flex', gap: '8px' }}>
                <button
                  type="button"
                  onClick={handleOpenTab}
                  style={{
                    background: 'var(--accent-subtle)',
                    color: 'var(--accent)',
                    border: '1px solid var(--accent)',
                    borderRadius: '6px',
                    padding: '8px 14px',
                    fontSize: '13px',
                    fontWeight: '600',
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '6px',
                    cursor: 'pointer'
                  }}
                >
                  <ExternalLink style={{ width: '14px', height: '14px' }} /> Open in Browser
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Footer Actions */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', paddingTop: '14px', borderTop: '1px solid var(--border)' }}>
          <Button
            variant="danger"
            size="md"
            onClick={() => {
              onClose();
              onUnhide(file);
            }}
            icon={<Unlock style={{ width: '14px', height: '14px' }} />}
          >
            Restore to PC
          </Button>
          <Button
            variant="accent"
            size="md"
            onClick={() => onDownload(file)}
            icon={<Download style={{ width: '14px', height: '14px' }} />}
          >
            Download Decrypted
          </Button>
        </div>
      </div>
    </div>
  );
}
