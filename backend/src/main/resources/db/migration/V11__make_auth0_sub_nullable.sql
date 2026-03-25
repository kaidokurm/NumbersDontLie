-- ============================================
-- PHASE 2: Fix Auth0 Sub Constraint for Dual Auth
-- V11 Migration: Make auth0_sub nullable
-- ============================================
-- 
-- When supporting both Auth0 and email/password auth,
-- auth0_sub should be nullable since email/password users won't have it.
-- 
-- Previously: auth0_sub TEXT NOT NULL UNIQUE
-- Now: auth0_sub TEXT UNIQUE (NULL allowed for email/password users)

ALTER TABLE users
ALTER COLUMN auth0_sub DROP NOT NULL;
