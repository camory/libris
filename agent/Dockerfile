# Libris agent sandbox: everything one loop iteration needs, nothing from the host.
# JDK 21 (backend), Node 22 (frontend), git + gh (branches and PRs), claude (the agent).
FROM eclipse-temurin:21-jdk-noble

ARG UID=1000
ARG GID=1000
ARG NODE_MAJOR=22

ENV DEBIAN_FRONTEND=noninteractive \
    LANG=C.UTF-8 \
    LIBRIS_SANDBOX=1

# Base tools, GitHub CLI (official apt repo), Node.js (NodeSource).
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      ca-certificates curl git gnupg jq ripgrep unzip zip less procps python3 openssh-client \
 && install -d -m 0755 /etc/apt/keyrings \
 && curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg \
      -o /etc/apt/keyrings/githubcli-archive-keyring.gpg \
 && chmod go+r /etc/apt/keyrings/githubcli-archive-keyring.gpg \
 && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" \
      > /etc/apt/sources.list.d/github-cli.list \
 && curl -fsSL https://deb.nodesource.com/setup_${NODE_MAJOR}.x | bash - \
 && apt-get update \
 && apt-get install -y --no-install-recommends gh nodejs \
 && apt-get clean && rm -rf /var/lib/apt/lists/*

# Non-root user matching the host uid so bind-mounted files keep their owner.
# claude refuses --dangerously-skip-permissions as root, which is a feature.
RUN existing="$(getent passwd "${UID}" | cut -d: -f1)"; [ -z "$existing" ] || userdel -r "$existing"; \
    getent group "${GID}" >/dev/null || groupadd -g "${GID}" agent; \
    useradd -m -u "${UID}" -g "${GID}" -s /bin/bash agent
USER agent
ENV HOME=/home/agent \
    PATH=/home/agent/.local/bin:${PATH}

# Claude Code native binary, installed for the agent user.
RUN curl -fsSL https://claude.ai/install.sh | bash \
 && claude --version

# Non-interactive git defaults; identity comes from env at run time.
RUN git config --global init.defaultBranch main \
 && git config --global pull.ff only \
 && git config --global advice.detachedHead false \
 && git config --global credential.helper '!gh auth git-credential'

# Deliberately absent until a task needs it (decided 2026-09-07): Playwright
# and a browser. When the first end-to-end task reports blocked, add
#   RUN npx -y playwright@<version> install --with-deps chromium
# as root before `USER agent`, rebuild, rerun.

WORKDIR /work
ENTRYPOINT ["/bin/bash", "-lc"]
CMD ["claude --version"]
