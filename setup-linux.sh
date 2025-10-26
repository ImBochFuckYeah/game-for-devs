#!/bin/bash
# Script para configurar permisos de ejecución en Linux/macOS
# Ejecutar una sola vez después de clonar el repositorio

echo "======================================"
echo " Game For Devs - Configuración Linux"
echo "======================================"

echo ""
echo "Configurando permisos de ejecución para scripts..."

# Dar permisos a todos los scripts .sh
chmod +x *.sh

echo "✓ Permisos configurados para:"
ls -la *.sh | while read line; do
    filename=$(echo $line | awk '{print $9}')
    if [[ $filename == *.sh ]]; then
        echo "  - $filename"
    fi
done

echo ""
echo "======================================"
echo " Configuración completada!"
echo "======================================"
echo ""
echo "Ahora puedes ejecutar:"
echo "  ./deploy.sh   - Para despliegue inicial"  
echo "  ./update.sh   - Para actualizaciones"
echo ""
echo "💡 Solo necesitas ejecutar este script una vez"
echo "======================================"