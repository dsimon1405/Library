CREATE DATABASE IF NOT EXISTS library_auth_db_docker;
CREATE DATABASE IF NOT EXISTS library_user_db_docker;
CREATE DATABASE IF NOT EXISTS library_lib_db_docker;

CREATE USER 'auth_acc'@'%' IDENTIFIED BY 'auth_pass';
CREATE USER 'user_acc'@'%' IDENTIFIED BY 'user_pass';
CREATE USER 'lib_acc'@'%' IDENTIFIED BY 'lib_pass';

GRANT ALL PRIVILEGES ON library_auth_db_docker.* TO 'auth_acc'@'%';
GRANT ALL PRIVILEGES ON library_user_db_docker.* TO 'user_acc'@'%';
GRANT ALL PRIVILEGES ON library_lib_db_docker.* TO 'lib_acc'@'%';

FLUSH PRIVILEGES;
