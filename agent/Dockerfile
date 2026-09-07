# Libris agent sandbox: everything one loop iteration needs, nothing from the host.
# JDK 25 (backend), Node 24 (frontend), Contracteer CLI (mock server, D04),
# gitleaks (the guard hook's secret scan), git + gh (branches and PRs),
# claude (the agent).
FROM eclipse-temurin:25-jdk-noble

ARG UID=1000
ARG GID=1000
ARG NODE_MAJOR=24
ARG CONTRACTEER_VERSION=4.0.0
ARG CONTRACTEER_SHA256=781be856f0f1764c8cf6fa0936bc767ba5733d90f289780f066f33d7e0c7f52e
ARG GITLEAKS_VERSION=8.30.1
ARG GITLEAKS_SHA256=551f6fc83ea457d62a0d98237cbad105af8d557003051f41f3e7ca7b3f2470eb

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

# Contracteer CLI: native binary, pinned and checksummed (D04, D08).
RUN curl -fsSL -o /tmp/contracteer.zip \
      "https://github.com/contracteer-dev/contracteer/releases/download/${CONTRACTEER_VERSION}/contracteer-${CONTRACTEER_VERSION}-linux-x86_64.zip" \
 && echo "${CONTRACTEER_SHA256}  /tmp/contracteer.zip" | sha256sum -c - \
 && unzip -q /tmp/contracteer.zip -d /tmp \
 && install -m 0755 "/tmp/contracteer-${CONTRACTEER_VERSION}-linux-x86_64/bin/contracteer" /usr/local/bin/contracteer \
 && rm -rf /tmp/contracteer.zip "/tmp/contracteer-${CONTRACTEER_VERSION}-linux-x86_64" \
 && contracteer --version

# gitleaks: the guard hook refuses commits and pushes carrying a credential.
RUN curl -fsSL -o /tmp/gitleaks.tar.gz \
      "https://github.com/gitleaks/gitleaks/releases/download/v${GITLEAKS_VERSION}/gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz" \
 && echo "${GITLEAKS_SHA256}  /tmp/gitleaks.tar.gz" | sha256sum -c - \
 && tar -xzf /tmp/gitleaks.tar.gz -C /tmp gitleaks \
 && install -m 0755 /tmp/gitleaks /usr/local/bin/gitleaks \
 && rm -f /tmp/gitleaks.tar.gz /tmp/gitleaks \
 && gitleaks version

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
