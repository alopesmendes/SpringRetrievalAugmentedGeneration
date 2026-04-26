# Project Overview: Image RAG

## Overview
Image Retrieval Augmented Generation (RAG) system. Uses Spring AI to extract, store, and query information from image data.

## Problem
Unstructured image data is difficult to search, index, and retrieve using standard text-based queries.

## Goals
- Master RAG architecture and implementation.
- Deepen Spring Boot backend development expertise.
- Implement DevOps practices (CI/CD, containerization, observability).

## Non-Goals
- Commercialization or monetization.
- Scaling for massive production workloads; this is a personal learning project.
- Avoiding external costs (zero-cost infrastructure preference).

## Target User
Primarily developers and engineers interested in AI-integrated backend systems.

## Core Features
- **Identity & Access Management**:
    - Role-based access control (RBAST) for user management.
    - Hierarchical permissions (Admin vs. User) for token access and user lifecycle control.
- **AI Image Processing**:
    - Automated data extraction from images using AI models.
    - Vectorized storage of extracted metadata for semantic retrieval.
- **RAG Implementation**:
    /Natural language querying against image content (e.g., "Which of these 4 images contains the Eiffel Tower?").
- **Admin Intelligence**:
    - Global similarity search for images across the entire system.
    - Traceability: Identifying image origin/owner via administrative view.
- **Security & Privacy**:
    - Image storage in dedicated object storage.
    - Encrypted user data within relational databases to ensure privacy and protection.
- **User Lifecycle Management**:
    - Email-driven onboarding: Account creation request $\rightarrow$ Verification code $\rightarrow$ Confirmation.
    - Soft-deletion workflow: Accounts are deactivated for 1 week (allowing recovery) before permanent erasure.

## Metrics of Success
- Successful implementation of all core feature sets.
- Mastery of specified technical domains (Spring, AI, DevOps).
- Zero infrastructure cost incurred during development.
