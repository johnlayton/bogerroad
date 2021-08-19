# -*- mode: Python -*-

load('ext://helm_remote', 'helm_remote')
load('ext://pack', 'pack')

#helm_remote('mysql',
#            repo_name='stable',
#            repo_url='https://charts.helm.sh/stable')

# helm_remote('mariadb',
#             repo_name='bitnami',
#             repo_url='https://charts.bitnami.com/bitnami',
#             values='tilt/mariadb/kubernetes-mariadb-values.yaml')
# k8s_resource('mariadb', port_forwards=['3306:3306'])

helm_remote('postgresql',
            repo_name='bitnami',
            repo_url='https://charts.bitnami.com/bitnami',
            values='tilt/postgresql/kubernetes-postgresql-values.yaml')
k8s_resource(workload='postgresql-postgresql', port_forwards=['5432:5432'])

# helm_remote('jaeger',
#             repo_name='jaegertracing',
#             repo_url='https://jaegertracing.github.io/helm-charts')
# k8s_resource(workload='jaeger', port_forwards=['5432:5432'])

k8s_yaml('tilt/zipkin/kubernetes-zipkin.yaml')
k8s_resource('zipkin', port_forwards=['9411:9411'])

# k8s_yaml('tilt/jaeger/kubernetes-jaeger.yaml')
# k8s_resource('jaeger', port_forwards=['16686:16686','9411:9411'])

k8s_yaml('tilt/otel/kubernetes-otel.yaml')
k8s_resource('otel', port_forwards=['4317:4317', '9412:9412'])

# k8s_yaml('tilt/jaeger/kubernetes-jaeger.yaml')
# k8s_resource('jaeger', port_forwards=['16686:16686','9411:9411'])
#
# k8s_yaml('tilt/otel/kubernetes-otel.yaml')
# k8s_resource('otel', port_forwards=['4317:4317'])

# k8s_yaml('tilt/otel/kubernetes-otel-demo.yaml')
# k8s_resource('otel', port_forwards=['4317:4317','9411:9411', '16686:16686'])

k8s_yaml('tilt/kafka/kubernetes-zookeeper.yaml')
k8s_resource('zookeeper', port_forwards=['2181:2181'])

k8s_yaml('tilt/kafka/kubernetes-kafka.yaml')
k8s_resource('kafka', port_forwards=['9092'])

k8s_yaml('tilt/kafka/kubernetes-kafka-manager.yaml')
k8s_resource('kafka-manager', port_forwards=['9000', '9999'])

k8s_yaml('tilt/rabbitmq/kubernetes-rabbitmq.yaml')
k8s_resource('rabbitmq', port_forwards='15672')

custom_build('bulk-stream',
    'cd bulk-stream && ./gradlew --no-daemon bootBuildImage --imageName=$EXPECTED_REF',
    deps=['bulk-stream/src'])

k8s_yaml('tilt/stream/kubernetes-application.yaml')
k8s_resource('bulk-stream', port_forwards=['6565:6565', '8080:8080'])

# custom_build('bulk-trigger',
#     'cd bulk-trigger && ./gradlew --no-daemon bootBuildImage --imageName=$EXPECTED_REF',
#     deps=['bulk-trigger/src'])
#
# k8s_yaml('tilt/trigger/kubernetes-application.yaml')
# k8s_resource('bulk-trigger', port_forwards=['8081:8080'])

# custom_build('bulk-trace-api',
#     'cd bulk-trace/api && ./gradlew --no-daemon bootBuildImage --imageName=$EXPECTED_REF',
#     deps=['bulk-trace/ap/src'])
#
# # pack('bulk-trace-api', path = "bulk-trace/api")
# k8s_yaml('tilt/trace/kubernetes-api-application.yaml')
# k8s_resource('bulk-trace-api', port_forwards=8080)
