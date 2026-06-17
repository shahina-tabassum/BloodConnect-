FROM tomcat:9-jdk17-temurin

# Clean default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy built war file to the ROOT context
COPY target/bloodconnect.war /usr/local/tomcat/webapps/ROOT.war

# Copy and set entrypoint script
COPY entrypoint.sh /usr/local/tomcat/bin/entrypoint.sh
RUN chmod +x /usr/local/tomcat/bin/entrypoint.sh

# Set Entrypoint
ENTRYPOINT ["/usr/local/tomcat/bin/entrypoint.sh"]
