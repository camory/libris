FROM node:24.20.0-trixie-slim AS build
WORKDIR /workspace
COPY . .
RUN npm ci
ARG VERSION=dev
ENV VITE_APP_VERSION=$VERSION
RUN npm run build

FROM nginx:1.30.4-alpine3.24
ARG VERSION=dev
LABEL org.opencontainers.image.version=$VERSION
LABEL org.opencontainers.image.source=https://github.com/camory/libris
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist /usr/share/nginx/html
