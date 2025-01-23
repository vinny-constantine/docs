## command
```shell
### tailscale
docker run -d \
  --name=tailscaled \
  --restart=always      \
  --network=host \
  --cap-add=NET_ADMIN \
  --cap-add=NET_RAW \
  -e TS_AUTHKEY=tskey-client-k2nWrWUpAr11CNTRL-WsBCVVv9qweUQUDKNY4CwejURjow97enV \
  -e TS_ROUTES=0.0.0.0/24 \
  -e TS_STATE_DIR=/var/lib/tailscale \
  -v /volume1/docker/tailscal:/var/lib \
  --restart unless-stopped \
  tailscale/tailscale:latest


### zerotier
docker run -d           \
  --name=zerotier       \
  --restart=always      \
  --network=host        \
  --cap-add=NET_ADMIN   \
  --cap-add=SYS_ADMIN   \
  --device=/dev/net/tun \
  -v /volume1/docker/zerotier-one:/var/lib/zerotier-one \
  --restart unless-stopped \
  zerotier/zerotier:latest



### qbittorrent
docker run -d \
  --name=qbittorrent \
  -e PUID=1000 \
  -e PGID=1000 \
  -e TZ=Etc/UTC \
  -e WEBUI_PORT=58080 \
  -e TORRENTING_PORT=56881 \
  -p 58080:58080 \
  -p 56881:56881 \
  -p 56881:56881/udp \
  -v /volume1/docker/qbittorrent/appdata:/config \
  --mount type=bind,source=/volume1/docker/media/downloads,target=/downloads \
  --restart unless-stopped \
  linuxserver/qbittorrent:latest

### jellyfin

docker run -d \
 --name jellyfin \
 --net=host \
 --volume /volume1/docker/jellyfin/config:/config \
 --volume /volume1/docker/jellyfin/cache:/cache \
 --mount type=bind,source=/volume1/docker/media,target=/media \
 --mount type=bind,source=/volume1/docker/movie,target=/movie \
 --mount type=bind,source=/volume1/docker/music,target=/music \
 --restart=unless-stopped \
 nyanmisaka/jellyfin:latest

### jellyfin改密码，将 IsSetupWizardComplete = false
vim /volume1/docker/jellyfin/config/config/system.xml 


### moviepilot
docker run -d \
  --name moviepilot-v2 \
  --hostname moviepilot-v2 \
  --network host \
  -v /volume1/docker/moviepilot-v2/config:/config \
  -v /volume1/docker/moviepilot-v2/core:/moviepilot/.cache/ms-playwright \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  -e 'NGINX_PORT=3000' \
  -e 'PORT=3001' \
  -e 'PUID=0' \
  -e 'PGID=0' \
  -e 'UMASK=000' \
  -e 'TZ=Asia/Shanghai' \
  -e 'SUPERUSER=dover' \
  --mount type=bind,source=/volume1/docker/media,target=/media \
  --mount type=bind,source=/volume1/docker/movie,target=/movie \
  --restart unless-stopped \
  jxxghp/moviepilot-v2:latest

  # -e 'API_TOKEN=无需手动配置，系统会自动生成。如果需要自定义配置，必须为16位以上的复杂字符串' \
  # -e 'AUTH_SITE=v2.0.7+版本以后可不设置，直接通过 UI 配置' \
  # -e 'IYUU_SIGN=xxxx' \
```