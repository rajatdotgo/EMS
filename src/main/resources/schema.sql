# # DROP TABLE IF EXISTS employee;
# # DROP TABLE IF EXISTS designation;
#
#
# CREATE TABLE designation(
#                             DES_ID INT NOT NULL AUTO_INCREMENT,
#                             DESG_NAME VARCHAR(255) NOT NULL,
#                             LEVEL FLOAT,
#                             PRIMARY KEY (DES_ID)
# );
# CREATE TABLE employee (
#                           emp_id INT NOT NULL AUTO_INCREMENT,
#                           emp_name VARCHAR(255) NOT NULL,
#                           parent_id INT ,
#                           designation_des_id INT ,
#                           PRIMARY KEY (emp_id),
#                           FOREIGN KEY (designation_des_id) REFERENCES designation(DES_ID));


CREATE TABLE if not exists designation(
                                          DES_ID INT NOT NULL AUTO_INCREMENT,q
                                          DESG_NAME VARCHAR(255) NOT NULL,
                                          LEVEL FLOAT,
                                          PRIMARY KEY (DES_ID));


CREATE TABLE if not exists employee (
                                        emp_id INT NOT NULL AUTO_INCREMENT,
                                        emp_name VARCHAR(255) NOT NULL,
                                        parent_id INT ,
                                        designation_des_id INT ,
                                        PRIMARY KEY (emp_id),
                                        FOREIGN KEY (designation_des_id) REFERENCES designation(DES_ID));