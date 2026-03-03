#!/bin/bash
# 允许从库通过流复制连接主库
echo "host replication all 0.0.0.0/0 md5" >> "${PGDATA:-/var/lib/postgresql/data}/pg_hba.conf"
