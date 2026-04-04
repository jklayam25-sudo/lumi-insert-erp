ALTER TABLE auth_tokens_aud
    ALTER COLUMN expired_at DROP NOT NULL;
    
ALTER TABLE auth_tokens_aud
    ALTER COLUMN refresh_token DROP NOT NULL;
 