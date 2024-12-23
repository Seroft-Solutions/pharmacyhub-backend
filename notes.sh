sudo mkdir -p /var/www/pharmacyhub.pk/html

sudo chown -R $USER:$USER /var/www/pharmacyhub.pk/html

sudo chmod -R 755 /var/www/pharmacyhub.pk

sudo nano /var/www/pharmacyhub.pk/html/index.html

<html>
    <head>
        <title>Welcome to Pharmacy Hub</title>
    </head>
    <body>
        <h1>Success!  The Pharmacy Hub server block is working!</h1>
    </body>
</html>

sudo nano /etc/nginx/sites-available/pharmacyhub.pk

server {
    listen 80;
    server_name pharmacyhub.pk www.pharmacyhub.pk;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# HTTPS server configuration
server {
    listen 443 ssl;
    server_name pharmacyhub.pk www.pharmacyhub.pk;

    ssl_certificate /etc/nginx/ssl/pharmacyhub.pk.crt; # Path to your SSL certificate
    ssl_certificate_key /etc/nginx/ssl/pharmacyhub.pk.key; # Path to your SSL certificate key

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}


sudo ln -s /etc/nginx/sites-available/pharmacyhub.pk /etc/nginx/sites-enabled/
sudo nano /etc/nginx/nginx.conf

sudo certbot --nginx -d pharmacyhub.pk.com -d www.pharmacyhub.pk

ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root';

CREATE USER 'pharmacyhub'@'host' IDENTIFIED BY 'PharmacyHub#1';
GRANT PRIVILEGE ON database.table TO 'username'@'host';

GRANT CREATE, ALTER, DROP, INSERT, UPDATE, DELETE, SELECT, REFERENCES, RELOAD on *.* TO 'pharmacyhub'@'localhost' WITH GRANT OPTION;

GRANT ALL PRIVILEGES ON *.* TO 'pharmacyhub'@'localhost' WITH GRANT OPTION;