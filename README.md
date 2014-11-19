#J-Ace - Java Actual Impact Set (AIS) Analyser

The purpose of J-Ace is to perform Impact Analysis against software repositories AFTER the change has been made. Tools exist that support developers to determine the Estimated Impact Set before the implementation phase. The J-Ace tool aims to collect data from the Actual Impact Set and to provide information to the development team and especially the Quality Assurance about the software changes made to the actual features.

This tool is implemented as part of Master's Thesis as a one-man project (Master of Engineering, JAMK University of Applied Sciences, www.jamk.fi).


#Installation

**Prerequisites:**

* Git
* Maven
* Glassfish
* Postgresql
* Postgresql driver for Glassfish: http://jdbc.postgresql.org/download/postgresql-9.3-1102.jdbc41.jar (copy under domain/lib)


**Steps:**

1. Clone the repository
2. Open shell and go to /build dir
3. mvn package
4. Start up Glassfish and Postgresql database server
5. Add a new database jaceDb to Postgresql. Add user jace with jace password.
6. Configure the PostgreSQL pool in Glassfish management console to use jaceDb with aforementioned username and password
7. Setup the jdbc/jaceDs datasource and set it to use the Postgresql pool
8. Deploy the jace EAR from /ear/target
