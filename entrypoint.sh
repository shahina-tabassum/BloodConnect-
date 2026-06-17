#!/bin/bash
# Port binding mapping for environments like Railway that inject PORT env var
if [ -n "$PORT" ]; then
  echo "Replacing Tomcat default port 8080 with Railway PORT: $PORT"
  sed -i "s/port=\"8080\"/port=\"$PORT\"/g" /usr/local/tomcat/conf/server.xml
fi

# Execute Tomcat server
exec catalina.sh run
