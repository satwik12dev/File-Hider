import React, { useState, useMemo } from 'react';
import {
  FileText, HardDrive, ShieldCheck, Activity,
  Search, Grid, List, RefreshCw, Eye, Download, Unlock,
  UploadCloud, Clipboard, Lock, ArrowUpRight
} from 'lucide-react';
import { Card } from './ui/Card';
import { Button } from './ui/Button';
import { Input } from './ui/Input';
import { hideByPathApi, hideUploadApi, getFileDownloadUrl } from '../services/api';

export default function Dashboard({
  user,
  files,
  onRefreshFiles,
  onOpenPreview,
  onPromptUnhide,
  onShowToast
}) {
  const [activeTab, setActiveTab] = useState('path'); // 'path' | 'upload'
  const [localPath, setLocalPath] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [hideLoading, setHideLoading] = useState(false);

  const [searchQuery, setSearchQuery] = useState('');
  const [activeCategory, setActiveCategory] = useState('all');
  const [viewMode, setViewMode] = useState('grid'); // 'grid' | 'list'

  const totalFiles = files.length;
  const totalBytes = useMemo(() => {
    return files.reduce((acc, f) => acc + (f.size || 120000), 0);
  }, [files]);

  const formattedStorage = useMemo(() => {
    if (totalBytes < 1024 * 1024) {
      return `${(totalBytes / 1024).toFixed(1)} KB`;
    }
    return `${(totalBytes / (1024 * 1024)).toFixed(2)} MB`;
  }, [totalBytes]);

  const getFileCategory = (filename) => {
    const ext = filename ? filename.split('.').pop()?.toLowerCase() : '';
    if (['jpg', 'jpeg', 'png', 'gif', 'svg', 'webp', 'bmp'].includes(ext)) return 'image';
    if (['pdf', 'doc', 'docx', 'xls', 'xlsx', 'txt', 'rtf'].includes(ext)) return 'doc';
    if (['mp4', 'mov', 'mp3', 'wav'].includes(ext)) return 'media';
    if (['js', 'ts', 'jsx', 'tsx', 'html', 'css', 'json', 'py', 'java', 'sql', 'md'].includes(ext)) return 'code';
    if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return 'archive';
    return 'other';
  };

  const getCategoryEmoji = (category) => {
    switch (category) {
      case 'image': return '🖼️';
      case 'doc': return '📄';
      case 'media': return '🎬';
      case 'code': return '💻';
      case 'archive': return '🗜️';
      default: return '📁';
    }
  };

  const filteredFiles = useMemo(() => {
    return files.filter((f) => {
      const matchSearch =
        !searchQuery ||
        f.fileName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        f.path?.toLowerCase().includes(searchQuery.toLowerCase());

      const matchCategory =
        activeCategory === 'all' ||
        (activeCategory === 'document' && getFileCategory(f.fileName) === 'doc') ||
        (activeCategory === 'image' && getFileCategory(f.fileName) === 'image') ||
        (activeCategory === 'code' && getFileCategory(f.fileName) === 'code') ||
        (activeCategory === 'archive' && getFileCategory(f.fileName) === 'archive');

      return matchSearch && matchCategory;
    });
  }, [files, searchQuery, activeCategory]);

  const formatSize = (bytes) => {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
  };

  const handlePasteClipboard = async () => {
    try {
      const text = await navigator.clipboard.readText();
      if (text && text.trim()) {
        const cleaned = text.trim().replace(/^["']|["']$/g, '');
        setLocalPath(cleaned);
        onShowToast('Path pasted from clipboard', 'info');
      } else {
        onShowToast('Clipboard is empty', 'warning');
      }
    } catch {
      onShowToast('Press Ctrl+V to paste your file path', 'info');
    }
  };

  const handleHideByPath = async (e) => {
    e.preventDefault();
    const cleanPath = localPath.trim().replace(/^["']|["']$/g, '');
    if (!cleanPath) {
      onShowToast('Please enter a local file path', 'error');
      return;
    }

    setHideLoading(true);
    try {
      const res = await hideByPathApi(cleanPath, user?.email);
      setLocalPath('');
      await onRefreshFiles();
      onShowToast(res.message || 'File safely encrypted & wiped from PC', 'success');
    } catch (err) {
      onShowToast(err.message, 'error');
    } finally {
      setHideLoading(false);
    }
  };

  const handleHideUpload = async (e) => {
    e.preventDefault();
    if (!selectedFile) {
      onShowToast('Please select a file to conceal', 'error');
      return;
    }

    setHideLoading(true);
    try {
      const res = await hideUploadApi(selectedFile, user.email);
      onShowToast(res.message || 'File encrypted into vault storage', 'success');
      setSelectedFile(null);
      await onRefreshFiles();
    } catch (err) {
      onShowToast(err.message, 'error');
    } finally {
      setHideLoading(false);
    }
  };

  return (
    <div className="dashboard-view">
      {/* 4 Clean Stats */}
      <div className="dashboard-stats-row">
        <Card className="stat-box" interactive={false}>
          <div className="stat-header">
            <span className="stat-title">Concealed Files</span>
            <FileText style={{ width: '16px', height: '16px', color: 'var(--text-muted)' }} />
          </div>
          <div className="stat-value">{totalFiles}</div>
          <span className="stat-sub">Active encrypted assets</span>
        </Card>

        <Card className="stat-box" interactive={false}>
          <div className="stat-header">
            <span className="stat-title">Encrypted Storage</span>
            <HardDrive style={{ width: '16px', height: '16px', color: 'var(--text-muted)' }} />
          </div>
          <div className="stat-value">{formattedStorage}</div>
          <span className="stat-sub">Total database volume</span>
        </Card>

        <Card className="stat-box" interactive={false}>
          <div className="stat-header">
            <span className="stat-title">Encryption Cipher</span>
            <ShieldCheck style={{ width: '16px', height: '16px', color: 'var(--accent)' }} />
          </div>
          <div className="stat-value" style={{ fontSize: '18px', paddingTop: '4px' }}>
            AES-256-GCM
          </div>
          <span className="stat-sub">Zero-knowledge key</span>
        </Card>

        <Card className="stat-box" interactive={false}>
          <div className="stat-header">
            <span className="stat-title">Vault Status</span>
            <Activity style={{ width: '16px', height: '16px', color: 'var(--status-live)' }} />
          </div>
          <div className="stat-value" style={{ fontSize: '18px', color: 'var(--status-live)', paddingTop: '4px' }}>
            Protected
          </div>
          <span className="stat-sub">Disk wipe enabled</span>
        </Card>
      </div>

      {/* Conceal Console */}
      <Card className="console-box" interactive={false}>
        <div className="console-header">
          <div>
            <h2 className="console-title">Conceal New File</h2>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '2px' }}>
              Encrypt files into secure storage and unlink original from your computer
            </p>
          </div>

          <div className="console-tabs">
            <button
              type="button"
              onClick={() => setActiveTab('path')}
              className={`console-tab-btn ${activeTab === 'path' ? 'active' : ''}`}
            >
              Local Path
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('upload')}
              className={`console-tab-btn ${activeTab === 'upload' ? 'active' : ''}`}
            >
              File Upload
            </button>
          </div>
        </div>
        {!user?.token && (
          <div style={{
            margin: '0 0 16px 0',
            padding: '10px 14px',
            borderRadius: '8px',
            background: 'rgba(234, 179, 8, 0.08)',
            border: '1px solid rgba(234, 179, 8, 0.25)',
            color: '#facc15',
            fontSize: '12px',
            lineHeight: '1.5'
          }}>
            ⚠️ <strong>Offline Enclave Mode:</strong> You are currently in client-side preview mode. Files are stored in browser memory, but <strong>physical file wiping from your PC drive requires logging in to the active Spring Boot backend</strong>. Please click <strong>Lock</strong> (top-right) and log in to enable real disk wiping.
          </div>
        )}

        {activeTab === 'path' ? (
          <form onSubmit={handleHideByPath}>
            <div className="console-form-row">
              <div style={{ flex: 1 }}>
                <Input
                  value={localPath}
                  onChange={(e) => setLocalPath(e.target.value)}
                  placeholder="e.g. C:\Users\SATWIK\Documents\confidential.pdf"
                  required
                />
              </div>
              <Button
                variant="outline"
                size="md"
                onClick={handlePasteClipboard}
                icon={<Clipboard style={{ width: '14px', height: '14px' }} />}
              >
                Paste
              </Button>
              <Button
                type="submit"
                variant="accent"
                size="md"
                disabled={hideLoading}
                icon={<Lock style={{ width: '14px', height: '14px' }} />}
              >
                {hideLoading ? 'Concealing...' : 'Conceal & Wipe'}
              </Button>
            </div>

            <div className="tip-banner">
              💡 Tip: Right-click any file in File Explorer and select <strong>"Copy as path"</strong> (or press <code>Ctrl+Shift+C</code>), then click Paste.
            </div>
          </form>
        ) : (
          <form onSubmit={handleHideUpload}>
            <div
              style={{
                border: '1px dashed var(--border)',
                borderRadius: '12px',
                padding: '24px',
                textAlign: 'center',
                background: 'var(--bg-subtle)',
                position: 'relative',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '8px',
                cursor: 'pointer'
              }}
            >
              <input
                type="file"
                onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
                style={{ position: 'absolute', inset: 0, opacity: 0, cursor: 'pointer' }}
              />
              <UploadCloud style={{ width: '28px', height: '28px', color: 'var(--accent)' }} />
              <span style={{ fontSize: '13.5px', fontWeight: '600', color: 'var(--text-pure)' }}>
                {selectedFile ? selectedFile.name : 'Choose a file or drag and drop'}
              </span>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                {selectedFile ? `${formatSize(selectedFile.size)} selected` : 'Encrypts directly to database binary storage'}
              </span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '12px' }}>
              <Button
                type="submit"
                variant="accent"
                size="md"
                disabled={hideLoading || !selectedFile}
                icon={<Lock style={{ width: '14px', height: '14px' }} />}
              >
                {hideLoading ? 'Encrypting...' : 'Upload & Encrypt'}
              </Button>
            </div>
          </form>
        )}
      </Card>

      {/* Files Explorer */}
      <Card className="explorer-box" interactive={false}>
        <div className="explorer-header">
          <div>
            <h2 className="console-title">Vault Explorer</h2>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '2px' }}>
              {files.length} protected {files.length === 1 ? 'file' : 'files'} in your personal enclave
            </p>
          </div>

          <div className="explorer-search-wrap">
            <div style={{ position: 'relative', width: '220px' }}>
              <Search style={{ width: '14px', height: '14px', position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search files..."
                style={{
                  width: '100%',
                  background: 'var(--bg-subtle)',
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  padding: '6px 10px 6px 30px',
                  fontSize: '12.5px',
                  color: 'var(--text-pure)',
                  outline: 'none'
                }}
              />
            </div>

            {/* View mode */}
            <div style={{ display: 'flex', gap: '2px', background: 'var(--bg-subtle)', border: '1px solid var(--border)', borderRadius: '8px', padding: '2px' }}>
              <button
                type="button"
                onClick={() => setViewMode('grid')}
                style={{
                  background: viewMode === 'grid' ? 'var(--bg-card-solid)' : 'none',
                  color: viewMode === 'grid' ? 'var(--text-pure)' : 'var(--text-muted)',
                  border: 'none',
                  padding: '5px 8px',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center'
                }}
                title="Grid"
              >
                <Grid style={{ width: '14px', height: '14px' }} />
              </button>
              <button
                type="button"
                onClick={() => setViewMode('list')}
                style={{
                  background: viewMode === 'list' ? 'var(--bg-card-solid)' : 'none',
                  color: viewMode === 'list' ? 'var(--text-pure)' : 'var(--text-muted)',
                  border: 'none',
                  padding: '5px 8px',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center'
                }}
                title="List"
              >
                <List style={{ width: '14px', height: '14px' }} />
              </button>
            </div>

            <Button
              variant="outline"
              size="sm"
              onClick={async () => {
                await onRefreshFiles();
                onShowToast?.('Vault inventory synchronized', 'info');
              }}
              icon={<RefreshCw style={{ width: '13px', height: '13px' }} />}
            >
              Sync
            </Button>
          </div>
        </div>

        {/* Filter Chips */}
        <div className="explorer-filters">
          {[
            { id: 'all', label: 'All Files' },
            { id: 'document', label: 'Documents' },
            { id: 'image', label: 'Media' },
            { id: 'code', label: 'Code' },
            { id: 'archive', label: 'Archives' }
          ].map((c) => (
            <button
              key={c.id}
              type="button"
              onClick={() => setActiveCategory(c.id)}
              className={`filter-chip ${activeCategory === c.id ? 'active' : ''}`}
            >
              {c.label}
            </button>
          ))}
        </div>

        {/* Files View */}
        {filteredFiles.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '48px 0', color: 'var(--text-muted)' }}>
            <p style={{ fontSize: '14px', fontWeight: '600', color: 'var(--text-secondary)' }}>
              {files.length === 0 ? 'No concealed files yet' : 'No files matching search'}
            </p>
            <p style={{ fontSize: '12px', marginTop: '4px' }}>
              {files.length === 0 ? 'Use the console above to conceal your first confidential file.' : 'Try clearing your search filter.'}
            </p>
          </div>
        ) : viewMode === 'grid' ? (
          <div className="files-grid-cards">
            {filteredFiles.map((file) => (
              <Card key={file.id} className="file-card-inner">
                <div className="file-row-top">
                  <div className="file-icon-box">
                    {getCategoryEmoji(getFileCategory(file.fileName))}
                  </div>
                  <div className="file-meta-col">
                    <h3 className="file-name-text" title={file.fileName}>{file.fileName}</h3>
                    <p className="file-path-text" title={file.path || 'Encrypted DB Blob'}>
                      {file.path || 'Encrypted Storage Blob'}
                    </p>
                  </div>
                </div>

                <div className="file-card-footer">
                  <span>ID #{file.id}</span>
                  <span>{formatSize(file.size)}</span>
                </div>

                <div className="file-actions-row">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onOpenPreview(file)}
                  >
                    View
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => {
                      window.location.href = getFileDownloadUrl(file.id);
                      onShowToast(`Downloading ${file.fileName}...`, 'info');
                    }}
                  >
                    Download
                  </Button>
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => onPromptUnhide(file)}
                  >
                    Restore
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        ) : (
          <div style={{ overflowX: 'auto', marginTop: '16px' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px', textAlign: 'left' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border)', color: 'var(--text-muted)', fontSize: '12px' }}>
                  <th style={{ padding: '10px 12px' }}>ID</th>
                  <th style={{ padding: '10px 12px' }}>Name</th>
                  <th style={{ padding: '10px 12px' }}>Original Path</th>
                  <th style={{ padding: '10px 12px' }}>Size</th>
                  <th style={{ padding: '10px 12px', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredFiles.map((file) => (
                  <tr key={file.id} style={{ borderBottom: '1px solid var(--border)' }}>
                    <td style={{ padding: '10px 12px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', fontSize: '12px' }}>#{file.id}</td>
                    <td style={{ padding: '10px 12px', fontWeight: '600', color: 'var(--text-pure)' }}>
                      <span style={{ marginRight: '6px' }}>{getCategoryEmoji(getFileCategory(file.fileName))}</span>
                      {file.fileName}
                    </td>
                    <td style={{ padding: '10px 12px', color: 'var(--text-muted)', fontSize: '12px', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={file.path}>
                      {file.path || 'Encrypted DB Blob'}
                    </td>
                    <td style={{ padding: '10px 12px', color: 'var(--text-secondary)', fontSize: '12px' }}>{formatSize(file.size)}</td>
                    <td style={{ padding: '10px 12px', textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '4px' }}>
                        <Button variant="outline" size="sm" onClick={() => onOpenPreview(file)}>View</Button>
                        <Button variant="outline" size="sm" onClick={() => {
                          window.location.href = getFileDownloadUrl(file.id);
                          onShowToast(`Downloading ${file.fileName}...`, 'info');
                        }}>Download</Button>
                        <Button variant="danger" size="sm" onClick={() => onPromptUnhide(file)}>Restore</Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
