./keytool -k ./okandroid.keystore -p okandroid -pk8 platform.pk8 -cert platform.x509.pem -alias okandroid
#-k 表示要生成的 keystore 文件的名字，这里命名为 platform.keystore
#-p 表示要生成的 keystore 的密码，这里是 android
#-pk8 表示要导入的 platform.pk8 文件
#-cert 表示要导入的platform.x509.pem
#-alias 表示给生成的 platform.keystore 取一个别名，这是命名为 platform

#~output:
#Importing "okandroid" with SHA256 Fingerprint=2D:37:0C:21:F5:DF:D5:53:D2:A7:96:31:4B:70:92:5F:B3:8A:DE:EF:90:86:4C:92:0B:BB:BB:12:88:7D:35:22
#Importing keystore /tmp/keytool.bu3P/p12 to ./okandroid.keystore...
#Entry for alias okandroid successfully imported.
#Import command completed:  1 entries successfully imported, 0 entries failed or cancelled
