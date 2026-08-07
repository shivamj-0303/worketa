-- Ensure unique organisation email
ALTER TABLE organisations
    ADD CONSTRAINT uq_organisations_email UNIQUE (email);

-- Ensure user email unique per organisation
ALTER TABLE users
    ADD CONSTRAINT uq_users_org_email UNIQUE (organisation_id, email);
