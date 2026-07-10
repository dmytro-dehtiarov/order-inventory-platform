# Oracle Database VM Setup (Manual, Optional)

> **Most contributors do not need this.** If you just want to run and test the project, use
> `docker compose up -d` as described in the [README](../README.md) — it gets you a working
> Oracle instance in under two minutes with no manual configuration.
>
> This document is for the alternative path: installing a full Oracle Database server on a
> dedicated VM, the way the author originally set it up. It's useful if you want a persistent,
> full-featured Oracle server to work against — for example, if you're also learning Oracle
> database administration alongside this project — but it's overkill for simply running the
> app.

## Overview

Steps below install Oracle Database Free on Oracle Linux 8, running in a VM (VMware or
VirtualBox), reachable from the host machine over a bridged network connection.

## 1. Install Oracle Linux 8

Install Oracle Linux 8 as a guest OS in VMware or VirtualBox.

## 2. Install the preinstall package

```bash
dnf install oracle-database-preinstall
```

This sets up the kernel parameters, users, and groups that the Oracle Database RPM expects.

## 3. Install the Oracle Free RPM

Download and install the Oracle Free 26ai RPM package on the VM.

## 4. Configure the database

```bash
/etc/init.d/oracle-free-26ai configure
```

Follow the prompts to set the database passwords and complete initial configuration.

## 5. Configure the listener

Edit `listener.ora` so the listener accepts connections from outside the VM, not just localhost:

```
HOST=0.0.0.0
```

## 6. Configure networking

- Set the VM's network adapter to **Bridged** mode so it gets an address on the host's LAN
  rather than a NAT'd/internal address.
- Assign a static IP via `nmcli`, e.g.:

```bash
nmcli connection modify ens160 ipv4.addresses 192.168.x.x/24
nmcli connection modify ens160 ipv4.method manual
```

(Replace `ens160` with your actual interface name and use an address appropriate for your LAN.)

## 7. Open the database port in the firewall

```bash
firewall-cmd --permanent --add-port=1521/tcp
firewall-cmd --reload
```

## 8. Create the application user

Connect as a privileged user and create a dedicated schema/user for the application:

```sql
ALTER SESSION SET CONTAINER = FREEPDB1;
CREATE USER oip_app IDENTIFIED BY "your_password";
GRANT CONNECT, RESOURCE TO oip_app;
GRANT UNLIMITED TABLESPACE TO oip_app;
```

Use this user's credentials as `DB_USER` / `DB_PASSWORD` in your `.env`.

## 9. Enable autostart

So the database and networking come back up automatically after a VM reboot:

```bash
systemctl enable oracle-free-26ai
nmcli connection modify ens160 connection.autoconnect yes
```

## Connecting from the app

Once the VM is reachable, point `.env` at it (see `.env.example`):

```
DB_HOST=<vm-static-ip>
DB_PORT=1521
DB_SERVICE=FREEPDB1
DB_USER=oip_app
DB_PASSWORD=<the password you set in step 8>
```
