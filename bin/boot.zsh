#!/usr/bin/env zsh


kenv destroy && \
  kenv create minikube && \
  cd . && cd . && \
  tilt up
