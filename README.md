# Virtual Hosts (Apache Server)
> Simple Script for creating Apache VirtualHosts (with error checking)

## Installing
```bash
$ git clone https://github.com/malusev998/Apache_VirtualHosts.git
```

# Global Using

## Setup script globaly
```bash
$ sudo mv virtualhost.sh /usr/local/bin/virtualhost
```
## Make script executable
```bash
$ cd /usr/local/bin && sudo chmod +x virtualhost
```

Using the script
```bash
$ sudo virtualhost (create | delete) example.com
```

# Local using
## Make script executable
```bash
$  sudo chmod +x virtualhost.sh
```

Using the script
```bash
$ sudo ./virtualhost.sh (create | delete) example.com
```
## Plan to add powershell core script

