FROM fluent/fluentd:v1.14.5-debian-1.0

USER root

# you may customize including plugins as you wish
# libffi-dev net-tools"
RUN buildDeps="sudo make gcc g++ libc-dev" \
 && apt-get update \
 && apt-get upgrade -y \
 && apt-get install -y --no-install-recommends $buildDeps \
 && gem uninstall -I elasticsearch && gem install elasticsearch -v 7.17.0 \
 && gem install fluent-plugin-elasticsearch --no-document --version 5.0.3 \
 && gem install fluent-plugin-parser-cri --no-document \
 && gem install fluent-plugin-concat --no-document \
 && gem install fluent-plugin-kubernetes_metadata_filter \
 && gem install fluent-plugin-multi-format-parser \
 && gem sources --clear-all \
 && SUDO_FORCE_REMOVE=yes \
    apt-get purge -y --auto-remove \
                  -o APT::AutoRemove::RecommendsImportant=false \
                  $buildDeps \
 && rm -rf /var/lib/apt/lists/* \
 && rm -rf /tmp/* /var/tmp/* /usr/lib/ruby/gems/*/cache/*.gem \
 && echo "Done..."
