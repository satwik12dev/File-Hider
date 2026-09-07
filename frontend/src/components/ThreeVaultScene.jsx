import React, { useEffect, useRef } from 'react';
import * as THREE from 'three';

export default function ThreeVaultScene({ onTriggerAction }) {
  const mountRef = useRef(null);

  useEffect(() => {
    const container = mountRef.current;
    if (!container) return;

    const width = container.clientWidth || 360;
    const height = container.clientHeight || 260;

    // Scene setup
    const scene = new THREE.Scene();

    // Camera
    const camera = new THREE.PerspectiveCamera(40, width / height, 0.1, 100);
    camera.position.set(0, 0, 7.5);

    // Renderer with high quality anti-aliasing
    const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
    renderer.setSize(width, height);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 1.1;
    container.appendChild(renderer.domElement);

    // Group for the 3D vault centerpiece
    const vaultGroup = new THREE.Group();
    scene.add(vaultGroup);

    // 1. Sleek Outer Floating Vault Cube (Beveled/Rounded look via BoxGeometry + Wireframe)
    const boxGeo = new THREE.BoxGeometry(2.4, 2.4, 2.4);
    const boxMat = new THREE.MeshPhysicalMaterial({
      color: 0x18181f,
      metalness: 0.85,
      roughness: 0.25,
      clearcoat: 0.6,
      clearcoatRoughness: 0.1,
      reflectivity: 0.9,
      wireframe: false
    });
    const mainBox = new THREE.Mesh(boxGeo, boxMat);
    vaultGroup.add(mainBox);

    // Subtle edge highlight for clean luxury definition
    const edgeGeo = new THREE.EdgesGeometry(boxGeo);
    const edgeMat = new THREE.LineBasicMaterial({
      color: 0x6366f1,
      transparent: true,
      opacity: 0.4
    });
    const boxEdges = new THREE.LineSegments(edgeGeo, edgeMat);
    vaultGroup.add(boxEdges);

    // 2. Central Metallic Vault Dial / Safe Rotor
    const dialGeo = new THREE.CylinderGeometry(0.75, 0.75, 0.25, 32);
    const dialMat = new THREE.MeshStandardMaterial({
      color: 0x272730,
      metalness: 0.9,
      roughness: 0.2
    });
    const dial = new THREE.Mesh(dialGeo, dialMat);
    dial.rotation.x = Math.PI / 2;
    dial.position.z = 1.2;
    vaultGroup.add(dial);

    // Inner dial ring
    const innerRingGeo = new THREE.TorusGeometry(0.55, 0.04, 16, 48);
    const ringMat = new THREE.MeshStandardMaterial({
      color: 0x6366f1,
      emissive: 0x4f46e5,
      emissiveIntensity: 0.4,
      metalness: 0.8,
      roughness: 0.2
    });
    const innerRing = new THREE.Mesh(innerRingGeo, ringMat);
    innerRing.position.z = 1.34;
    vaultGroup.add(innerRing);

    // Center lock emblem/point
    const corePointGeo = new THREE.SphereGeometry(0.18, 16, 16);
    const corePointMat = new THREE.MeshStandardMaterial({
      color: 0x10b981,
      emissive: 0x10b981,
      emissiveIntensity: 0.5,
      roughness: 0.1
    });
    const corePoint = new THREE.Mesh(corePointGeo, corePointMat);
    corePoint.position.z = 1.35;
    vaultGroup.add(corePoint);

    // 3. Subtle floating orbital orbit ring
    const orbitRingGeo = new THREE.TorusGeometry(2.1, 0.02, 16, 64);
    const orbitMat = new THREE.MeshBasicMaterial({
      color: 0x818cf8,
      transparent: true,
      opacity: 0.25
    });
    const orbitRing = new THREE.Mesh(orbitRingGeo, orbitMat);
    orbitRing.rotation.x = Math.PI / 3;
    vaultGroup.add(orbitRing);

    // Studio Lighting
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.8);
    scene.add(ambientLight);

    const keyLight = new THREE.DirectionalLight(0xffffff, 2.2);
    keyLight.position.set(5, 6, 6);
    scene.add(keyLight);

    const rimLight = new THREE.DirectionalLight(0x6366f1, 3.0);
    rimLight.position.set(-6, -4, -3);
    scene.add(rimLight);

    const fillLight = new THREE.PointLight(0x10b981, 1.2, 10);
    fillLight.position.set(0, 2, 3);
    scene.add(fillLight);

    // Mouse Tracking
    let mouseX = 0;
    let mouseY = 0;
    let targetRotationX = 0.25;
    let targetRotationY = -0.35;

    const handleMouseMove = (e) => {
      const rect = container.getBoundingClientRect();
      const x = ((e.clientX - rect.left) / rect.width) * 2 - 1;
      const y = -(((e.clientY - rect.top) / rect.height) * 2 - 1);
      mouseX = x;
      mouseY = y;
    };

    window.addEventListener('mousemove', handleMouseMove);

    // Animation loop
    let animationId;
    let clock = new THREE.Clock();

    const animate = () => {
      animationId = requestAnimationFrame(animate);
      const elapsed = clock.getElapsedTime();

      // Gentle floating bob
      vaultGroup.position.y = Math.sin(elapsed * 1.5) * 0.12;

      // Gentle continuous rotation + smooth mouse tilt
      targetRotationY = -0.35 + mouseX * 0.5 + elapsed * 0.15;
      targetRotationX = 0.25 - mouseY * 0.4;

      vaultGroup.rotation.y += (targetRotationY - vaultGroup.rotation.y) * 0.05;
      vaultGroup.rotation.x += (targetRotationX - vaultGroup.rotation.x) * 0.05;

      // Slowly rotate the safe dial and orbital ring
      dial.rotation.z = elapsed * 0.4;
      orbitRing.rotation.z = -elapsed * 0.2;

      renderer.render(scene, camera);
    };

    animate();

    const handleResize = () => {
      if (!container) return;
      const newW = container.clientWidth;
      const newH = container.clientHeight;
      camera.aspect = newW / newH;
      camera.updateProjectionMatrix();
      renderer.setSize(newW, newH);
    };

    window.addEventListener('resize', handleResize);

    return () => {
      cancelAnimationFrame(animationId);
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('resize', handleResize);
      if (container && renderer.domElement) {
        container.removeChild(renderer.domElement);
      }
      renderer.dispose();
    };
  }, []);

  return (
    <div
      ref={mountRef}
      onClick={onTriggerAction}
      style={{
        width: '100%',
        height: '260px',
        cursor: 'pointer',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}
      title="Click to interact with 3D Vault"
    />
  );
}
