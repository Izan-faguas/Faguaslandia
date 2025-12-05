#!/bin/bash
# Uso: ./wait-for-mysql.sh host comando_a_ejecutar
host="$1"
shift
cmd="$@"

echo "Esperando a que MySQL esté listo en $host..."

until mysql -h "$host" -u "$DB_USER" -p"$DB_PASSWORD" -e 'SELECT 1;' &> /dev/null
do
  echo "MySQL aún no está disponible..."
  sleep 2
done

echo "MySQL está listo, iniciando Apache..."
exec $cmd

