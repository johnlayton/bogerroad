#!/usr/bin/env zsh

export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
export POSTGRES_USER=dbuser
export POSTGRES_PASSWORD=dbpass
cd bulk-backstage && yarn dev
