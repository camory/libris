# Deploying Libris on Gordien

`compose.yaml` runs PostgreSQL, the backend and the frontend behind the
server's Traefik. Everything below happens on Gordien in the folder that
holds this file, written `$LIBRIS_HOME` here, where every compose command is
run.

## Outside this repository, once

**DNS.** An `A` record `libris.amory.fr` pointing at the server.

**Traefik**, in the dynamic configuration files. Two routers on the same
host, the `/api` one to the backend, both behind the Authelia middleware. The
Actuator endpoints are not routed.

```yaml
http:
  routers:
    libris:
      rule: Host(`libris.amory.fr`)
      entryPoints: [websecure]
      middlewares: [<authelia-middleware>]
      tls: { certResolver: <resolver> }
      service: libris-frontend
    libris-api:
      rule: Host(`libris.amory.fr`) && PathPrefix(`/api`)
      entryPoints: [websecure]
      middlewares: [<authelia-middleware>]
      tls: { certResolver: <resolver> }
      service: libris-backend
  services:
    libris-frontend:
      loadBalancer:
        servers: [{ url: http://libris-frontend:8080 }]
    libris-backend:
      loadBalancer:
        servers: [{ url: http://libris-backend:8080 }]
```

The Authelia forward-auth middleware must pass the four headers the backend
reads:

```yaml
authResponseHeaders: [Remote-User, Remote-Groups, Remote-Name, Remote-Email]
```

**Authelia.** An access rule for every household user, and the group whose
members administer the catalogue:

```yaml
access_control:
  rules:
    - domain: libris.amory.fr
      policy: <policy>
```

```yaml
users:
  <user>:
    groups: [libris-admin]
```

**Backups.** In Gordien's backup job, the two lines for this service: the
folder `$LIBRIS_HOME`, and the dump command

```sh
docker compose exec -T postgres pg_dump -U libris --clean --if-exists libris
```

## First deploy

```sh
mkdir -p $LIBRIS_HOME && cd $LIBRIS_HOME
# copy compose.yaml and .env.example from deploy/ in the repository
cp .env.example .env      # set LIBRIS_TAG and LIBRIS_DB_PASSWORD
docker compose pull
docker compose up -d
docker compose ps         # backend and postgres healthy
docker compose exec backend curl -fsS localhost:8080/actuator/info
```

Then in a browser: <https://libris.amory.fr> shows the Authelia login, then the
home page greeting the reader by name, with the footer revision equal to
`git rev-parse --short <tag>` in the repository.

Images are public on ghcr.io; no login is needed. Every release tag exists for
both images at once.

## Upgrade

```sh
cd $LIBRIS_HOME
docker compose exec -T postgres pg_dump -U libris --clean --if-exists libris > pre-upgrade.sql
# edit .env: LIBRIS_TAG=<new tag>
docker compose pull
docker compose up -d
docker compose ps
```

The backend applies its database migrations at boot. A migration that fails
keeps the backend from starting: `docker compose logs backend`.

## Rollback

Set the previous tag in `.env`, then `docker compose pull && docker compose
up -d`. If the version rolled back from added a migration, restore
`pre-upgrade.sql` first, as below.

## Restore from a dump

```sh
cd $LIBRIS_HOME
docker compose stop backend
docker compose exec -T postgres psql -U libris -d libris < <dump>.sql
docker compose start backend
```

Dumps carry `--clean --if-exists`, so they restore into the existing
database. Run the backend tag that produced the dump, or a later one.

## When it fails

- `docker compose ps` and `docker compose logs -f <service>`.
- A 401 or a redirect to the login page comes from Authelia, before Libris.
- A 502 on `libris.amory.fr` means Traefik cannot reach the container: check
  that the container is on the `web` network and its name is the one in the
  Traefik service.
- The backend answers `/actuator/health` inside its container only:
  `docker compose exec backend curl -fsS localhost:8080/actuator/health`.
