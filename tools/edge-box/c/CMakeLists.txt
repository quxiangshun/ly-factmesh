cmake_minimum_required(VERSION 3.14)
project(factmesh_edge_box C)

set(CMAKE_C_STANDARD 99)

# 查找 paho.mqtt.c
# Linux: apt install libpaho-mqtt-dev
# macOS: brew install paho-mqtt
# vcpkg: vcpkg install paho-mqtt
find_path(PAHO_MQTT_INCLUDE_DIR NAMES MQTTClient.h
    PATHS /usr/include /usr/local/include
    DOC "paho.mqtt.c include directory")
find_library(PAHO_MQTT_LIB NAMES paho-mqtt3c paho-mqtt3as
    PATHS /usr/lib /usr/local/lib
    DOC "paho.mqtt.c library")

if(NOT PAHO_MQTT_INCLUDE_DIR OR NOT PAHO_MQTT_LIB)
    message(FATAL_ERROR
        "paho.mqtt.c not found. Install:\n"
        "  Linux:   sudo apt install libpaho-mqtt-dev\n"
        "  macOS:   brew install paho-mqtt\n"
        "  vcpkg:   vcpkg install paho-mqtt")
endif()

add_executable(factmesh_edge_box src/main.c)

target_include_directories(factmesh_edge_box PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/src
    ${PAHO_MQTT_INCLUDE_DIR})

target_link_libraries(factmesh_edge_box PRIVATE ${PAHO_MQTT_LIB})

# Windows 需要 ws2_32
if(WIN32)
    target_link_libraries(factmesh_edge_box PRIVATE ws2_32)
endif()
