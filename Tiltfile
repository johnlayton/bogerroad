# -*- mode: Python -*-
#
load('ext://tilt_inspector', 'tilt_inspector')
load('ext://configmap', 'configmap_create', 'configmap_from_dict')
load('ext://secret', 'secret_yaml_generic', 'secret_from_dict')
load('ext://helm_remote', 'helm_remote')
load('ext://pack', 'pack')

# tilt_inspector()
analytics_settings(enable=False)

#helm_remote('mysql',
#            repo_name='stable',
#            repo_url='https://charts.helm.sh/stable')

# helm_remote('mariadb',
#             repo_name='bitnami',
#             repo_url='https://charts.bitnami.com/bitnami',
#             values='tilt/mariadb/kubernetes-mariadb-values.yaml')
# k8s_resource('mariadb', port_forwards=['3306:3306'])

# helm_remote('mailhog',
#             repo_name='codecentric',
#             repo_url='https://codecentric.github.io/helm-charts',
#             values='tilt/mailhog/kubernetes-mailhog-values.yaml')
# k8s_resource(new_name='mailhog', workload='mailhog', port_forwards=['8025:8025', '1025:1025'])

k8s_yaml('tilt/mailhog/kubernetes-mailhog-application.yaml')
k8s_resource('mailhog', port_forwards=['1025:1025', '8025:8025'])

helm_remote('postgresql',
            repo_name='bitnami',
            repo_url='https://charts.bitnami.com/bitnami',
            values='tilt/postgresql/kubernetes-postgresql-values.yaml')
k8s_resource(new_name='postgresql', workload='postgresql-postgresql', port_forwards=['5432:5432'])

# helm_remote('jaeger',
#             repo_name='jaegertracing',
#             repo_url='https://jaegertracing.github.io/helm-charts')
# k8s_resource(workload='jaeger', port_forwards=['5432:5432'])

# k8s_yaml('tilt/zipkin/kubernetes-zipkin.yaml')
# k8s_resource('zipkin', port_forwards=['9411:9411'])

# k8s_yaml('tilt/jaeger/kubernetes-jaeger.yaml')
# k8s_resource('jaeger', port_forwards=['16686:16686','9411:9411'])

# k8s_yaml('tilt/otel/kubernetes-otel.yaml')
# k8s_resource('otel', port_forwards=['4317:4317', '9412:9412'])

# k8s_yaml('tilt/otel/kubernetes-otel-demo.yaml')
# k8s_resource('otel', port_forwards=['4317:4317','9411:9411', '16686:16686'])

# k8s_yaml('tilt/kafka/kubernetes-zookeeper.yaml')
# k8s_resource('zookeeper', port_forwards=['2181:2181'])
#
# k8s_yaml('tilt/kafka/kubernetes-kafka.yaml')
# k8s_resource('kafka', port_forwards=['9092'])
#
# k8s_yaml('tilt/kafka/kubernetes-kafka-manager.yaml')
# k8s_resource('kafka-manager', port_forwards=['9000', '9999'])

k8s_yaml('tilt/rabbitmq/kubernetes-rabbitmq.yaml')
k8s_resource('rabbitmq', port_forwards='15672')

custom_build('bulk-plan-api',
    './gradlew --no-daemon bulk-plan:api:bootBuildImage --imageName=$EXPECTED_REF',
    deps=['bulk-plan/api/src'])
k8s_yaml('tilt/plan/api/kubernetes-application.yaml')
k8s_resource('bulk-plan-api', port_forwards=['6565:6565', '8080:8080'])

custom_build('bulk-plan-engine',
             './gradlew --no-daemon bulk-plan:engine:bootBuildImage --imageName=$EXPECTED_REF',
             deps=['bulk-plan/engine/src'])
k8s_yaml('tilt/plan/engine/kubernetes-application.yaml')
k8s_resource('bulk-plan-engine')

# custom_build('bulk-stream',
#     './gradlew --no-daemon bulk-stream:bootBuildImage --imageName=$EXPECTED_REF',
#     deps=['bulk-stream/src'])
# k8s_yaml('tilt/stream/kubernetes-application.yaml')
# k8s_resource('bulk-stream', port_forwards=['6565:6565', '8080:8080'])

# custom_build('bulk-cloud-template-consumer',
#     './gradlew --no-daemon bulk-cloud:template-consumer:bootBuildImage --imageName=$EXPECTED_REF',
#     deps=['bulk-cloud/template-consumer/src'])
# k8s_yaml('tilt/cloud/template-consumer/kubernetes-application.yaml')
# k8s_resource('bulk-cloud-template-consumer')

# custom_build('bulk-trigger',
#     'cd bulk-trigger && ./gradlew --no-daemon bootBuildImage --imageName=$EXPECTED_REF',
#     deps=['bulk-trigger/src'])
# k8s_yaml('tilt/trigger/kubernetes-application.yaml')
# k8s_resource('bulk-trigger', port_forwards=['8081:8080'])

# custom_build('bulk-trace-api',
#     './gradlew --no-daemon bulk-trace:api:bootBuildImage --imageName=$EXPECTED_REF',
#     deps=['bulk-trace/api/src'])
# k8s_yaml('tilt/trace/kubernetes-api-application.yaml')
# k8s_resource('bulk-trace-api', port_forwards=['8081:8080'])

# k8s_yaml(secret_from_dict("github", inputs={
#     'GITHUB_TOKEN': os.getenv("GITHUB_TOKEN"),
#     'AUTH_GITHUB_CLIENT_ID': os.getenv("AUTH_GITHUB_CLIENT_ID"),
#     'AUTH_GITHUB_CLIENT_SECRET': os.getenv("AUTH_GITHUB_CLIENT_SECRET")
# }))
# custom_build('bulk-backstage',
#              'cd bulk-backstage && yarn clean && yarn install --frozen-lockfile && yarn tsc && yarn build && yarn build-image --tag $EXPECTED_REF',
#              deps=['bulk-backstage/packages/backend/src'])
# k8s_yaml('tilt/backstage/kubernetes-backstage-application.yaml')
# k8s_resource('bulk-backstage', port_forwards=['7000:7000'])

# k8s_yaml(secret_from_dict("server", inputs={
#     'secret_key_base': "8273f56e9a2722fd91855f9a0a30bc7f757517d1d0e63010eec12648c827f3031858c5d0cec4da9406f0116f3d7d59a4073799f10c9315a6e3641f8c2705a8f5",
#     'lockbox_key': "4ab859506217cbea3edcc51a68e9dc6fb542875f9aa660ffedb39443c2d2cfe1"
# }))
# k8s_yaml('tilt/tooljet/kubernetes-tooljet-application.yaml')
# k8s_resource('tooljet', port_forwards=['3000:3000'])
