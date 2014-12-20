CREATE TABLE IF NOT EXISTS account
(
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(254) NOT NULL,
  email VARCHAR(254) NOT NULL,
  password VARCHAR(254) NOT NULL,
  activated_at TIMESTAMP,
  suspended_at TIMESTAMP,
  login_count INT NOT NULL,
  failed_login_count INT NOT NULL,
  locked_out_until TIMESTAMP,
  current_login_at TIMESTAMP,
  last_login_at TIMESTAMP,
  current_login_ip VARCHAR(254),
  last_login_ip VARCHAR(254),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  reset_token VARCHAR(254),
  reset_requested_at TIMESTAMP,
  CONSTRAINT idx_acc_id_series_remember_token UNIQUE (email)
);

CREATE INDEX email_name_idx ON account(email, name);


CREATE TABLE IF NOT EXISTS picture (
  pic_id BIGSERIAL PRIMARY KEY,
  pic_url VARCHAR(254) NOT NULL,
  account_id BIGINT NOT NULL,
  FOREIGN KEY (account_id) REFERENCES account (id)
);
