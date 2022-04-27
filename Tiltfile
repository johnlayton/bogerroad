# -*- mode: Python -*-
#
load('ext://tilt_inspector', 'tilt_inspector')
load('ext://configmap', 'configmap_create', 'configmap_from_dict')
load('ext://secret', 'secret_yaml_generic', 'secret_from_dict')
load('ext://helm_remote', 'helm_remote')
load('ext://pack', 'pack')

# tilt_inspector()
analytics_settings(enable=False)

docker_prune_settings(num_builds=2, max_age_mins=30, keep_recent=2)

# update_settings(suppress_unused_image_warnings=["timetable-service"])
# update_settings(suppress_unused_image_warnings=["fluentd"])
update_settings(max_parallel_updates=3, k8s_upsert_timeout_secs=240)

os.putenv('TILT_CACHE_DIR', os.path.abspath('./.tilt-cache'))
os.putenv('TILT_HELM_REMOTE_CACHE_DIR', os.path.abspath('./.tilt-helm'))
os.putenv('TILT_COREOS_PROMETHEUS_TEMP_DIR', os.path.abspath('./.tilt-coreos'))

load('ext://configmap', 'configmap_create', 'configmap_from_dict')
load('ext://secret', 'secret_yaml_generic', 'secret_from_dict')
load('ext://pack', 'pack')
load('ext://helm_remote', 'helm_remote')
load('ext://helm_resource', 'helm_resource', 'helm_repo')
load('ext://namespace', 'namespace_create')
load('ext://coreos_prometheus', 'setup_monitoring', 'get_prometheus_resources', 'get_prometheus_dependencies')
load('ext://cert_manager', 'deploy_cert_manager')

###
# Setup Certificate Manager
###

deploy_cert_manager()

###
# Setup Helm Repository
###

helm_repo('bitnami', 'https://charts.bitnami.com/bitnami')
# helm_repo('codecentric', 'https://codecentric.github.io/helm-charts')

###
# Setup Prometheus
###

setup_monitoring()

###
# Setup Elastic + Kibana using the Custom Resource Definition
###

namespace_create('elastic-system')
k8s_yaml(local('curl --silent --show-error --location https://download.elastic.co/downloads/eck/2.0.0/crds.yaml',
               quiet=True))
k8s_yaml(local('curl --silent --show-error --location https://download.elastic.co/downloads/eck/2.0.0/operator.yaml',
               quiet=True))

k8s_yaml('tilt/elastic/elastic.yaml')
k8s_yaml('tilt/elastic/kibana.yaml')

k8s_resource(
    new_name='elasticsearch',
    objects=['quickstart:elasticsearch'],
    extra_pod_selectors=[{
        'elasticsearch.k8s.elastic.co/cluster-name': 'quickstart'
    }],
    resource_deps=['elastic-operator'],
    port_forwards=9200
)
k8s_resource(
    new_name='kibana',
    objects=['quickstart:kibana'],
    extra_pod_selectors=[{
        'kibana.k8s.elastic.co/name': 'quickstart'
    }],
    labels=['logging'],
    resource_deps=['elastic-operator', 'elasticsearch'],
    port_forwards=5601
)
local_resource(
    'kibana-password',
    'echo "    USER: elastic";' +
    'echo "PASSWORD: $(kubectl get secret quickstart-es-elastic-user -o=jsonpath="{.data.elastic}" | base64 --decode)";',
    trigger_mode=TRIGGER_MODE_MANUAL,
    labels=['logging'],
    auto_init=False
)

docker_build('thirdparty/fluentd', 'tilt/fluentd')
k8s_yaml('tilt/fluentd/fluentd-config.yaml')
k8s_yaml('tilt/fluentd/fluentd-rbac.yaml')
k8s_yaml('tilt/fluentd/fluentd-daemonset.yaml')

###
# Setup Tracing
###

# namespace_create('cert-manager')
# k8s_yaml(local('curl --silent --show-error --location https://github.com/cert-manager/cert-manager/releases/download/v1.6.2/cert-manager.yaml'))

namespace_create('observability')
k8s_yaml(local(
    'curl --silent --show-error --location https://github.com/jaegertracing/jaeger-operator/releases/download/v1.31.0/jaeger-operator.yaml'))

k8s_yaml('tilt/jaeger/jaeger.yaml')

k8s_resource(
    new_name='jaeger',
    objects=['simplest:jaeger'],
    extra_pod_selectors=[{
        'app.kubernetes.io/name': 'simplest'
    }],
    labels=['tracing'],
    resource_deps=['jaeger-operator'],
    port_forwards=['16686', '9411']
)

###
# Setup PostgreSQL
###

# helm_remote('postgresql',
#             repo_name='bitnami',
#             repo_url='https://charts.bitnami.com/bitnami',
#             values='tilt/postgresql/kubernetes-postgresql-values.yaml')
helm_resource(
    'postgresql',
    'bitnami/postgresql',
    flags=[
        '-f', './tilt/postgresql/kubernetes-postgresql-values.yaml',
    ],
    labels=['external-services'],
    resource_deps=get_prometheus_dependencies()
)
k8s_resource(
    'postgresql',
    port_forwards=['5432:5432']
)

###
# Setup GraphQL
###

custom_build(
    'bulk-graph-simple',
    './gradlew --parallel bulk-graph:simple:bootBuildImage --imageName=$EXPECTED_REF',
    deps=['bulk-graph/simple/src', 'bulk-graph/simple/build.gradle']
)
helm_resource(
    'bulk-graph-simple',
    'charts/springboot',
    image_deps=['bulk-graph-simple'],
    image_keys=[
        ('image.repository', 'image.tag')
    ],
    flags=[
        '-f', './tilt/graph/simple/values.yaml',
    ],
    labels=['graph'],
    resource_deps=get_prometheus_dependencies()
)
k8s_resource(
    'bulk-graph-simple',
    port_forwards=['8080:8080', '8081:8081'],
    trigger_mode=TRIGGER_MODE_MANUAL,
    resource_deps=get_prometheus_dependencies()
)

# helm_remote('mysql',
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

# k8s_yaml('tilt/mailhog/kubernetes-mailhog-application.yaml')
# k8s_resource('mailhog', port_forwards=['1025:1025', '8025:8025'])
#
# helm_remote('postgresql',
#             repo_name='bitnami',
#             repo_url='https://charts.bitnami.com/bitnami',
#             values='tilt/postgresql/kubernetes-postgresql-values.yaml')
# k8s_resource(new_name='postgresql', workload='postgresql-postgresql', port_forwards=['5432:5432'])

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

# k8s_yaml('tilt/rabbitmq/kubernetes-rabbitmq.yaml')
# k8s_resource('rabbitmq', port_forwards='15672')

# custom_build('bulk-plan-api',
#     './gradlew --no-daemon bulk-plan:api:bootBuildImage --imageName=$EXPECTED_REF',
#     deps=['bulk-plan/api/src'])
# k8s_yaml('tilt/plan/api/kubernetes-application.yaml')
# k8s_resource('bulk-plan-api', port_forwards=['6565:6565', '8080:8080'])
#
# custom_build('bulk-plan-engine',
#              './gradlew --no-daemon bulk-plan:engine:bootBuildImage --imageName=$EXPECTED_REF',
#              deps=['bulk-plan/engine/src'])
# k8s_yaml('tilt/plan/engine/kubernetes-application.yaml')
# k8s_resource('bulk-plan-engine')

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
