// FactMesh 边缘盒子（Go）- 边缘端数据采集与上报
//
// 运行：go run . [-b broker] [-d device_id] [-i interval_sec]
// 构建：go build -o factmesh_edge_box .
package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"os"
	"os/signal"
	"syscall"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

const (
	topicPrefixInput = "factmesh/input/device/"
	defaultBroker    = "tcp://127.0.0.1:1883"
	defaultDeviceID  = "edge-box-01"
	defaultInterval  = 5
	defaultQoS       = 1
)

type payload struct {
	DeviceID    string  `json:"deviceId"`
	Timestamp   int64   `json:"timestamp"`
	Temperature float64 `json:"temperature"`
	Humidity    float64 `json:"humidity"`
}

func buildPayload(deviceID string) ([]byte, error) {
	now := time.Now().Unix()
	temp := 20.0 + float64(now%150)/10.0
	hum := 40.0 + float64(now%400)/10.0
	p := payload{
		DeviceID:    deviceID,
		Timestamp:   now,
		Temperature: temp,
		Humidity:    hum,
	}
	return json.Marshal(p)
}

func run(broker, deviceID string, interval int) error {
	clientID := "factmesh-edge-go-" + deviceID
	topic := topicPrefixInput + deviceID

	opts := mqtt.NewClientOptions().
		AddBroker(broker).
		SetClientID(clientID).
		SetKeepAlive(30 * time.Second).
		SetCleanSession(false)

	client := mqtt.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		return fmt.Errorf("MQTT connect failed: %w", token.Error())
	}
	defer client.Disconnect(1000)

	fmt.Printf("Connected to %s, publishing to %s every %ds\n", broker, topic, interval)

	ticker := time.NewTicker(time.Duration(interval) * time.Second)
	defer ticker.Stop()

	sigCh := make(chan os.Signal, 1)
	signal.Notify(sigCh, syscall.SIGINT, syscall.SIGTERM)

	for {
		select {
		case <-sigCh:
			return nil
		case <-ticker.C:
			body, err := buildPayload(deviceID)
			if err != nil {
				fmt.Fprintf(os.Stderr, "build payload: %v\n", err)
				continue
			}
			token := client.Publish(topic, byte(defaultQoS), false, body)
			if token.Wait() && token.Error() != nil {
				fmt.Fprintf(os.Stderr, "publish failed: %v\n", token.Error())
				continue
			}
			fmt.Printf("[%d] Published %d bytes to %s\n", time.Now().Unix(), len(body), topic)
		}
	}
}

func main() {
	broker := flag.String("b", defaultBroker, "MQTT broker address")
	deviceID := flag.String("d", defaultDeviceID, "Device ID")
	interval := flag.Int("i", defaultInterval, "Publish interval (seconds)")
	flag.Parse()

	if *interval < 1 {
		*interval = 1
	}

	fmt.Printf("FactMesh Edge Box (Go) - broker=%s device=%s interval=%ds\n", *broker, *deviceID, *interval)
	if err := run(*broker, *deviceID, *interval); err != nil {
		fmt.Fprintf(os.Stderr, "%v\n", err)
		os.Exit(1)
	}
}
