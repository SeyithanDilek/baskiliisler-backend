#!/bin/bash

# Baskılı İşler Backend - Docker Build & Run Script

echo "🚀 Baskılı İşler Backend Docker Build & Run Script"
echo "================================================="

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Mevcut container'ları durdur ve temizle
cleanup() {
    print_info "Mevcut container'ları temizleniyor..."
    docker-compose down --remove-orphans
    print_success "Temizlik tamamlandı"
}

# Docker build işlemi
build_app() {
    print_info "Docker image build ediliyor..."
    docker-compose build --no-cache
    if [ $? -eq 0 ]; then
        print_success "Build işlemi başarılı!"
    else
        print_error "Build işlemi başarısız!"
        exit 1
    fi
}

# Docker run işlemi
run_app() {
    print_info "Uygulama başlatılıyor..."
    docker-compose up -d
    if [ $? -eq 0 ]; then
        print_success "Uygulama başarıyla başlatıldı!"
        echo ""
        print_info "📊 Servis URL'leri:"
        echo "   🌐 Backend API: http://localhost:8088"
        echo "   📚 Swagger UI: http://localhost:8088/swagger-ui.html"
        echo "   🔗 API Docs: http://localhost:8088/api-docs"
        echo "   ❤️  Health Check: http://localhost:8088/actuator/health"
        echo ""
        print_info "📊 Database Bilgileri:"
        echo "   🐘 PostgreSQL: localhost:5432"
        echo "   📊 Database: baskiliisler"
        echo "   👤 Username: postgres"
        echo "   🔐 Password: password"
        echo ""
        print_info "📱 Logları görmek için: docker-compose logs -f backend"
    else
        print_error "Uygulama başlatılamadı!"
        exit 1
    fi
}

# PgAdmin başlat
run_with_admin() {
    print_info "PgAdmin ile birlikte başlatılıyor..."
    docker-compose --profile admin up -d
    if [ $? -eq 0 ]; then
        print_success "Uygulama ve PgAdmin başarıyla başlatıldı!"
        echo ""
        print_info "📊 Ek Servisler:"
        echo "   🗄️  PgAdmin: http://localhost:5050"
        echo "      📧 Email: admin@baskiliisler.com"
        echo "      🔐 Password: admin123"
    fi
}

# Container status göster
show_status() {
    print_info "Container durumları:"
    docker-compose ps
}

# Logs göster
show_logs() {
    print_info "Backend logları gösteriliyor... (Çıkmak için Ctrl+C)"
    docker-compose logs -f backend
}

# Ana menu
main_menu() {
    echo ""
    echo "Ne yapmak istiyorsunuz?"
    echo "1) 🏗️  Build + Run (Sadece Backend + Database)"
    echo "2) 🏗️  Build + Run + PgAdmin"
    echo "3) 🧹 Cleanup (Container'ları durdur ve temizle)"
    echo "4) 📊 Status (Container durumlarını göster)"
    echo "5) 📜 Logs (Backend loglarını göster)"
    echo "6) 🚪 Exit"
    echo ""
    read -p "Seçiminizi yapın (1-6): " choice

    case $choice in
        1)
            cleanup
            build_app
            run_app
            ;;
        2)
            cleanup
            build_app
            run_with_admin
            ;;
        3)
            cleanup
            ;;
        4)
            show_status
            ;;
        5)
            show_logs
            ;;
        6)
            print_info "Script sonlandırılıyor..."
            exit 0
            ;;
        *)
            print_error "Geçersiz seçim! Lütfen 1-6 arası bir sayı girin."
            main_menu
            ;;
    esac
}

# Script başlangıcı
if [ "$1" = "--auto" ]; then
    cleanup
    build_app
    run_app
else
    main_menu
fi 