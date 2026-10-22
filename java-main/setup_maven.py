import urllib.request, zipfile, os

url = "https://maven.aliyun.com/repository/central/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"
dest = "C:/Users/Administrator/.m2/maven"
os.makedirs(dest, exist_ok=True)
zip_path = os.path.join(dest, "apache-maven-3.9.9-bin.zip")
print("Downloading Maven...")
urllib.request.urlretrieve(url, zip_path)
print("Extracting...")
with zipfile.ZipFile(zip_path) as z:
    z.extractall(dest)
maven_bin = os.path.join(dest, "apache-maven-3.9.9", "bin", "mvn")
print("Done. Maven at:", maven_bin)
