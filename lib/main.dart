import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}
class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Torch Service Example'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: () => startService(), // **Modified**
                child: Text('Start Service'),
              ),
              ElevatedButton(
                onPressed: () => stopService(), // **Modified**
                child: Text('Stop Service'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> startService() async {
    var platform = MethodChannel('com.example.torch_service');
    try {
      await platform.invokeMethod('startService');
    } on PlatformException catch (e) {
      print("Failed to start service: '${e.message}'.");
    }
  }

  Future<void> stopService() async {
    var platform = MethodChannel('com.example.torch_service');
    try {
      await platform.invokeMethod('stopService');
    } on PlatformException catch (e) {
      print("Failed to stop service: '${e.message}'.");
    }
  }
}
